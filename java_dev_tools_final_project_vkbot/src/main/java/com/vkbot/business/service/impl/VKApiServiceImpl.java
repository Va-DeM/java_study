package com.vkbot.business.service.impl;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Keyboard;
import com.vk.api.sdk.queries.messages.MessagesSendQueryWithUserIds;
import com.vkbot.business.service.VKApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;


@Slf4j
@RequiredArgsConstructor
public class VKApiServiceImpl implements VKApiService {
    private final VkApiClient vkApiClient;
    private final GroupActor groupActor;


    @Override
    public void sendMessage(Long userId, String message) throws ApiException, ClientException {
        sendMessage(userId, message, null);
    }

    @Override
    public void sendMessage(Long userId, String message, Keyboard keyboard) throws ApiException, ClientException {
        log.info("Sending message to user {}: {}", userId, message);
        int randomId = ThreadLocalRandom.current().nextInt();
        try {
            MessagesSendQueryWithUserIds query = vkApiClient.messages()
                    .sendUserIds(groupActor)
                    .userId(userId)
                    .message(message)
                    .randomId(randomId);
            if (keyboard != null) {
                query = query.keyboard(keyboard);
            }
            query.execute();
        } catch (ClientException e) {
            handleSendError(e, userId, message);
        }
    }

    /**
     * Обрабатывает ошибку парсинга ответа от VK.
     * Для SDK 1.0.x ошибка вида "Can't parse json response: {\"response\":123}"
     * означает, что сообщение успешно отправлено, просто SDK не умеет читать простой ID.
     */
    private void handleSendError(ClientException e, Long userId, String message) throws ClientException {
        String msg = e.getMessage();

        if (msg != null &&
                msg.contains("Can't parse json response") &&
                msg.contains("\"response\"")) {

            log.warn("Message likely sent successfully, but SDK failed to parse VK response. " +
                            "This is a known limitation of VK Java SDK 1.x. User={}, MessageLength={}",
                    userId, message != null ? message.length() : 0);
            // Считаем отправку успешной, ничего не пробрасываем дальше
            return;
        }
        // Если ошибка другая — пробрасываем её дальше, чтобы приложение могло среагировать
        throw e;
    }
}

