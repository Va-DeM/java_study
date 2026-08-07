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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class UpdateTaskCommand implements BotCommand {
    private static final Map<Long, SearchTask> userEditingTasks = new HashMap<>();
    private static final Map<Long, List<SearchTask>> pendingSelection = new HashMap<>();

    private final VKApiService vkApiService;
    private final SearchTaskService searchTaskService;
    private final UserService userService;

    @Override
    public boolean supports(String input) {
        return input.equalsIgnoreCase("обновить задачу на поиск") ||
               input.equalsIgnoreCase("/update");
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing UpdateTaskCommand for user: {}", message.getUserId());

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
                    "📭 У вас нет активных заявок для обновления.");
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        if (tasks.size() == 1) {
            selectForEditing(vkApiService, message.getUserId(), tasks.get(0));
        } else {
            pendingSelection.put(message.getUserId(), tasks);

            StringBuilder response = new StringBuilder("Выберите заявку для обновления:\n\n");
            for (int i = 0; i < tasks.size(); i++) {
                response.append(i + 1).append(". Заявка #").append(tasks.get(i).getId()).append("\n");
            }
            response.append("\nОтправьте номер заявки для обновления.");

            try {
                vkApiService.sendMessage(message.getUserId(), response.toString(),
                        KeyboardFactory.numberedSelection(tasks.size()));
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_TASK;
    }

    public static SearchTask getEditingTask(Long userId) {
        return userEditingTasks.get(userId);
    }

    public static void removeEditingTask(Long userId) {
        userEditingTasks.remove(userId);
    }

    public static List<SearchTask> getPendingSelection(Long userId) {
        return pendingSelection.get(userId);
    }

    public static void clearPendingSelection(Long userId) {
        pendingSelection.remove(userId);
    }

    public static void selectForEditing(VKApiService vkApiService, Long userId, SearchTask task) {
        userEditingTasks.put(userId, task);
        KeywordCommand.clearAwaitingFreeText(userId);
        showTaskDetails(vkApiService, userId, task);
    }

    private static void showTaskDetails(VKApiService vkApiService, Long userId, SearchTask task) {
        StringBuilder response = new StringBuilder("✏️ Обновление заявки #").append(task.getId()).append("\n\n");
        response.append("Текущие параметры:\n");

        if (task.getKeyword() != null) {
            response.append("🔍 Ключевое слово: ").append(task.getKeyword()).append("\n");
        } else {
            response.append("🔍 Ключевое слово: не установлено\n");
        }

        if (task.getRegionCode() != null) {
            response.append("📍 Регион: ").append(RegionUtil.getRegionName(task.getRegionCode())).append("\n");
        } else {
            response.append("📍 Регион: не установлен\n");
        }

        if (task.getMinExperience() != null && task.getMinExperience() > 0) {
            response.append("📅 Опыт: от ").append(task.getMinExperience()).append(" лет\n");
        } else {
            response.append("📅 Опыт: не установлен\n");
        }

        if (task.getMinSalary() != null && task.getMinSalary() > 0) {
            response.append("💰 Зарплата: от ").append(task.getMinSalary()).append(" руб.\n");
        } else {
            response.append("💰 Зарплата: не установлена\n");
        }

        response.append("\nВыберите параметр из списка ниже для изменения или используйте команды:\n");
        response.append("/region [код] - изменить регион\n");
        response.append("/minExp [лет] - изменить опыт\n");
        response.append("/minSalary [сумма] - изменить зарплату\n");
        response.append("/keyword [слово] - изменить ключевое слово\n");
        response.append("/done - сохранить изменения");

        try {
            vkApiService.sendMessage(userId, response.toString(), KeyboardFactory.taskEditMenu(task));
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }
}
