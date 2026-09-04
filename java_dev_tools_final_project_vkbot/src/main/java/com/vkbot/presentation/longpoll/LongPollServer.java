package com.vkbot.presentation.longpoll;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.util.JsonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LongPollServer {
    private static final String FAILED = "failed";
    private static final String TS = "ts";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    private final VkApiClient vkApiClient;
    private final GroupActor groupActor;
    private final long groupId; // используем long, чтобы избежать проблем с Math.toIntExact

    @Getter
    private URI server;
    @Getter
    private String key;
    @Getter
    private String ts;

    public LongPollServer(VkApiClient vkApiClient, GroupActor groupActor, long groupId) {
        this.vkApiClient = vkApiClient;
        this.groupActor = groupActor;
        this.groupId = groupId;
    }

    public void init() throws ApiException, ClientException {
        log.info("Initializing LongPoll Server...");

        var response = vkApiClient.groups().getLongPollServer(groupActor).execute();

        this.server = response.getServer();
        this.key = response.getKey();
        this.ts = response.getTs();

        log.info("LongPoll Server initialized successfully!");
        log.info("DEBUG: server={}, key_start={}, ts={}",
                this.server,
                key != null ? key.substring(0, Math.min(16, key.length())) : "null",
                this.ts);
    }

    public List<MessageDTO> poll() throws Exception {
        List<MessageDTO> messages = new ArrayList<>();

        try {
            String url = String.format("%s?act=a_check&key=%s&ts=%s&wait=25", server, key, ts);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            if (responseBody != null && responseBody.startsWith("<")) {
                log.error("VK returned HTML instead of JSON. Likely 403/401. Preview: {}",
                        responseBody.substring(0, Math.min(200, responseBody.length())));
                throw new RuntimeException("VK API returned HTML page (error). Check token and group_id.");
            }

            JsonNode root = objectMapper.readTree(responseBody);

            if (root.has(FAILED)) {
                int failedCode = root.get(FAILED).asInt();
                handleFailedCode(failedCode);
                return messages;
            }

            JsonNode t = root.get(TS);
            this.ts = (t != null) ? t.asText() : this.ts;

            JsonNode updates = root.get("updates");
            if (updates != null && updates.isArray()) {
                for (JsonNode update : updates) {
                    try {
                        MessageDTO message = parseUpdate(update);
                        if (message != null) {
                            messages.add(message);
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing update: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during LongPoll: {}", e.getMessage(), e);
            throw e;
        }

        return messages;
    }

    /**
     * Парсит одно событие из LongPoll и возвращает MessageDTO только если это:
     * - обычное сообщение (message_new) с текстом
     * - и НЕ сообщение от самого сообщества (чтобы не обрабатывать ответы бота как команды)
     * Package-private (не private) — чтобы тесты могли проверить разбор JSON напрямую,
     * без реального HTTP-запроса к LongPoll.
     */
    MessageDTO parseUpdate(JsonNode update) {
        String type = update.path("type").asText(null);
        log.debug("Parsing raw update: type={}, data={}", type, update.toString());

        JsonNode objectNode = update.get("object");
        if (objectNode == null) {
            log.debug("No 'object' field in update, skipping.");
            return null;
        }

        // 1. Пропускаем заведомо не командные события
        if ("message_typing_state".equals(type) ||
                "message_read".equals(type) ||
                "message_reply".equals(type)) {
            // message_reply — это ответ бота (или другого участника), не нужно его передавать в CommandDispatcher
            log.debug("Skipping non-command update: type={}", type);
            return null;
        }

        long userId = -1;
        long messageId = -1;
        String text = "";

        // Основной формат: object -> message
        JsonNode messageNode = objectNode.get("message");
        if (messageNode != null) {
            JsonNode idNode = messageNode.get("id");
            JsonNode fromIdNode = messageNode.get("from_id");

            if (idNode != null && fromIdNode != null) {
                messageId = idNode.asLong();
                userId = fromIdNode.asLong();

                JsonNode textNode = messageNode.get("text");
                if (textNode != null && textNode.isTextual()) {
                    text = textNode.asText();
                }
            }
        }

        // Фолбэк: старый формат (редко, но бывает)
        if (userId == -1) {
            JsonNode idNode = objectNode.get("id");
            JsonNode fromIdNode = objectNode.get("from_id");

            if (idNode != null && fromIdNode != null) {
                messageId = idNode.asLong();
                userId = fromIdNode.asLong();

                JsonNode textNode = objectNode.get("text");
                if (textNode != null && textNode.isTextual()) {
                    text = textNode.asText();
                }
            }
        }

        // Если не смогли вытащить from_id — это не сообщение для обработки
        if (userId == -1) {
            log.debug("Skipping update, could not find user ID. Likely a typing/read/other event.");
            return null;
        }

        // 2. Пропускаем сообщения от самого сообщества (бот пишет сам себе)
        // В VK ID сообществ отрицательные: -groupId
        if (userId == -groupId) {
            log.debug("Skipping message from bot itself (from_id={}, groupId={})", userId, groupId);
            return null;
        }

        // 3. Пропускаем сообщения без текста (только вложения и т.д.), если не нужна такая логика
        if (text == null || text.isBlank()) {
            log.debug("Skipping message without text (only attachments), userId={}", userId);
            // Если хочешь обрабатывать сообщения только с вложениями — верни DTO без текста
            return null;
        }

        log.info(">>> SUCCESS! Message received: User={}, ID={}, Text='{}'", userId, messageId, text);
        return MessageDTO.builder()
                .userId(userId)
                .messageId(messageId)
                .text(text)
                .build();
    }

    private void handleFailedCode(int failedCode) throws ApiException, ClientException {
        log.warn("LongPoll failed with code: {}", failedCode);

        switch (failedCode) {
            case 1:
                log.info("History outdated, updating ts");
                break;
            case 2:
            case 3:
                log.info("Server or key expired, reconnecting...");
                init();
                break;
            default:
                log.error("Unknown LongPoll error code: {}", failedCode);
        }
    }

    public void reconnect() throws ApiException, ClientException {
        init();
    }
}