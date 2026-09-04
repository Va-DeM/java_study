package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Keyboard;
import com.vkbot.business.model.SearchTask;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class KeywordCommand implements BotCommand {
    private static final String MENU_LABEL = "слово для поиска";
    private static final Set<String> QUICK_OPTIONS = Set.of("Java", "Python", "JavaScript", "Frontend", "Backend");
    // Пользователи, только что нажавшие "Слово для поиска" — следующее произвольное
    // сообщение от них перехватит KeywordCaptureCommand и трактует как ключевое слово.
    private static final Set<Long> awaitingFreeText = new HashSet<>();

    private final VKApiService vkApiService;

    @Override
    public boolean supports(String input) {
        return input.startsWith("/keyword")
                || input.toLowerCase().startsWith(MENU_LABEL)
                || QUICK_OPTIONS.contains(input);
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing KeywordCommand for user: {}", message.getUserId());
        String text = message.getText();

        if (text.toLowerCase().startsWith(MENU_LABEL)) {
            awaitingFreeText.add(message.getUserId());
            send(message.getUserId(), "Выберите ключевое слово или введите своё текстом:",
                    KeyboardFactory.keywordOptions());
            return;
        }

        String keyword;
        if (QUICK_OPTIONS.contains(text)) {
            keyword = text;
        } else {
            String[] parts = text.split(" ", 2);
            if (parts.length < 2) {
                send(message.getUserId(), "❌ Пожалуйста, укажите ключевое слово. Пример: /keyword Java", null);
                return;
            }
            keyword = parts[1].trim();
        }

        if (keyword.isEmpty()) {
            send(message.getUserId(), "❌ Ключевое слово не может быть пустым.", null);
            return;
        }

        SearchTask task = NewTaskCommand.getUserTask(message.getUserId());
        if (task == null) {
            task = UpdateTaskCommand.getEditingTask(message.getUserId());
        }

        awaitingFreeText.remove(message.getUserId());

        if (task != null) {
            task.setKeyword(keyword);
            send(message.getUserId(), "✅ Ключевое слово установлено: " + keyword, KeyboardFactory.taskEditMenu(task));
        } else {
            send(message.getUserId(), "❌ Сначала создайте новую заявку или выберите задачу для обновления.", KeyboardFactory.mainMenu());
        }
    }

    private void send(Long userId, String text, Keyboard keyboard) {
        try {
            if (keyboard != null) {
                vkApiService.sendMessage(userId, text, keyboard);
            } else {
                vkApiService.sendMessage(userId, text);
            }
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.KEYWORD;
    }

    public static boolean isAwaitingFreeText(Long userId) {
        return awaitingFreeText.contains(userId);
    }

    public static void clearAwaitingFreeText(Long userId) {
        awaitingFreeText.remove(userId);
    }

    public static void captureFreeText(VKApiService vkApiService, Long userId, String rawText) {
        clearAwaitingFreeText(userId);
        String keyword = rawText.trim();

        if (keyword.isEmpty()) {
            sendStatic(vkApiService, userId, "❌ Ключевое слово не может быть пустым.", null);
            return;
        }

        SearchTask task = NewTaskCommand.getUserTask(userId);
        if (task == null) {
            task = UpdateTaskCommand.getEditingTask(userId);
        }

        if (task == null) {
            sendStatic(vkApiService, userId,
                    "❌ Сначала создайте новую заявку или выберите задачу для обновления.",
                    KeyboardFactory.mainMenu());
            return;
        }

        task.setKeyword(keyword);
        sendStatic(vkApiService, userId, "✅ Ключевое слово установлено: " + keyword,
                KeyboardFactory.taskEditMenu(task));
    }

    private static void sendStatic(VKApiService vkApiService, Long userId, String text, Keyboard keyboard) {
        try {
            if (keyboard != null) {
                vkApiService.sendMessage(userId, text, keyboard);
            } else {
                vkApiService.sendMessage(userId, text);
            }
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }
}
