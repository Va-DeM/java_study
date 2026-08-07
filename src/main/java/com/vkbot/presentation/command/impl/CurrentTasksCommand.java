package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.model.SearchTask;
import com.vkbot.business.service.SearchTaskService;
import com.vkbot.business.service.UserService;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import com.vkbot.util.RegionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CurrentTasksCommand implements BotCommand {
    private final VKApiService vkApiService;
    private final SearchTaskService searchTaskService;
    private final UserService userService;

    @Override
    public boolean supports(String input) {
        return input.equalsIgnoreCase("текущие заявки на поиск") ||
               input.equalsIgnoreCase("/current");
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing CurrentTasksCommand for user: {}", message.getUserId());

        if (!userService.exists(message.getUserId())) {
            try {
                vkApiService.sendMessage(message.getUserId(),
                    "❌ Вы не зарегистрированы. Используйте команду 'Начать' для регистрации.");
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        List<SearchTask> tasks = searchTaskService.findActiveByUserId(message.getUserId());

        if (tasks.isEmpty()) {
            try {
                vkApiService.sendMessage(message.getUserId(),
                    "📭 У вас нет активных заявок на поиск. Создайте новую с помощью команды 'Создать заявку на поиск вакансий'.",
                    KeyboardFactory.mainMenu());
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        StringBuilder response = new StringBuilder("📋 Ваши активные заявки на поиск:\n\n");

        for (int i = 0; i < tasks.size(); i++) {
            SearchTask task = tasks.get(i);
            response.append(i + 1).append(". Заявка #").append(task.getId()).append("\n");

            if (task.getKeyword() != null) {
                response.append("   🔍 Ключевое слово: ").append(task.getKeyword()).append("\n");
            }

            if (task.getRegionCode() != null) {
                response.append("   📍 Регион: ").append(RegionUtil.getRegionName(task.getRegionCode())).append("\n");
            }

            if (task.getMinExperience() != null && task.getMinExperience() > 0) {
                response.append("   📅 Опыт: от ").append(task.getMinExperience()).append(" лет\n");
            }

            if (task.getMinSalary() != null && task.getMinSalary() > 0) {
                response.append("   💰 Зарплата: от ").append(task.getMinSalary()).append(" руб.\n");
            }

            response.append("\n");
        }

        try {
            vkApiService.sendMessage(message.getUserId(), response.toString(), KeyboardFactory.mainMenu());
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.CURRENT_TASKS;
    }
}

