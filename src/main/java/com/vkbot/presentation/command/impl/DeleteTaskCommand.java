package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.model.SearchTask;
import com.vkbot.business.scheduler.TaskScheduler;
import com.vkbot.business.service.SearchTaskService;
import com.vkbot.business.service.UserService;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DeleteTaskCommand implements BotCommand {
    private static final Map<Long, List<SearchTask>> pendingDeletion = new HashMap<>();

    private final VKApiService vkApiService;
    private final SearchTaskService searchTaskService;
    private final UserService userService;
    private final TaskScheduler taskScheduler;

    @Override
    public boolean supports(String input) {
        return input.equalsIgnoreCase("удалить заявку на поиск") ||
               input.equalsIgnoreCase("/stop");
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing DeleteTaskCommand for user: {}", message.getUserId());

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
                    "📭 У вас нет активных заявок для удаления.");
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        if (tasks.size() == 1) {
            performDelete(vkApiService, searchTaskService, taskScheduler, message.getUserId(), tasks.get(0));
        } else {
            pendingDeletion.put(message.getUserId(), tasks);

            StringBuilder response = new StringBuilder("Выберите заявку для удаления:\n\n");
            for (int i = 0; i < tasks.size(); i++) {
                response.append(i + 1).append(". Заявка #").append(tasks.get(i).getId()).append("\n");
            }
            response.append("\nОтправьте номер заявки для удаления.");

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
        return CommandType.DELETE_TASK;
    }

    public static List<SearchTask> getPendingDeletion(Long userId) {
        return pendingDeletion.get(userId);
    }

    public static void clearPendingDeletion(Long userId) {
        pendingDeletion.remove(userId);
    }

    public static void performDelete(VKApiService vkApiService, SearchTaskService searchTaskService,
                                      TaskScheduler taskScheduler, Long userId, SearchTask task) {
        task.setActive(false);
        searchTaskService.delete(task.getId());
        taskScheduler.cancelTask(task.getId());

        try {
            vkApiService.sendMessage(userId, "✅ Заявка #" + task.getId() + " успешно удалена.",
                    KeyboardFactory.mainMenu());
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }
}
