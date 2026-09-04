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

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ExperienceCommand implements BotCommand {
    private static final String MENU_LABEL = "минимальный опыт";
    private static final Map<String, Integer> QUICK_OPTIONS = Map.of(
            "1 год", 1,
            "2 года", 2,
            "3 года", 3
    );

    private final VKApiService vkApiService;

    @Override
    public boolean supports(String input) {
        return input.startsWith("/minExp")
                || input.toLowerCase().startsWith(MENU_LABEL)
                || QUICK_OPTIONS.containsKey(input);
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing ExperienceCommand for user: {}", message.getUserId());
        String text = message.getText();

        if (text.toLowerCase().startsWith(MENU_LABEL)) {
            send(message.getUserId(), "Выберите минимальный опыт:", KeyboardFactory.experienceOptions());
            return;
        }

        Integer years = QUICK_OPTIONS.get(text);
        if (years == null) {
            String[] parts = text.split(" ", 2);
            if (parts.length < 2) {
                send(message.getUserId(), "❌ Пожалуйста, укажите количество лет опыта. Пример: /minExp 1", null);
                return;
            }
            try {
                years = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                send(message.getUserId(), "❌ Пожалуйста, введите корректное значение (число).", null);
                return;
            }
        }

        if (years < 0) {
            send(message.getUserId(), "❌ Опыт не может быть отрицательным.", null);
            return;
        }

        SearchTask task = NewTaskCommand.getUserTask(message.getUserId());
        if (task == null) {
            task = UpdateTaskCommand.getEditingTask(message.getUserId());
        }

        if (task != null) {
            task.setMinExperience(years);
            send(message.getUserId(), "✅ Минимальный опыт установлен: " + years + " лет", KeyboardFactory.taskEditMenu(task));
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
        return CommandType.MIN_EXP;
    }
}
