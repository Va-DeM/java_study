package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.model.SearchTask;
import com.vkbot.business.scheduler.TaskScheduler;
import com.vkbot.business.scheduler.VacancySearchJobFactory;
import com.vkbot.business.service.SearchTaskService;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import com.vkbot.util.RegionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DoneCommand implements BotCommand {
    private final VKApiService vkApiService;
    private final SearchTaskService searchTaskService;
    private final TaskScheduler taskScheduler;
    private final VacancySearchJobFactory jobFactory;

    @Override
    public boolean supports(String input) {
        return input.equalsIgnoreCase("готово") || input.equalsIgnoreCase("/done");
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing DoneCommand for user: {}", message.getUserId());

        SearchTask task = NewTaskCommand.getUserTask(message.getUserId());
        boolean isNewTask = task != null;

        if (!isNewTask) {
            task = UpdateTaskCommand.getEditingTask(message.getUserId());
        }

        if (task == null) {
            try {
                vkApiService.sendMessage(message.getUserId(),
                    "❌ Нет активной заявки для сохранения.");
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        searchTaskService.save(task);
        taskScheduler.scheduleTask(task.getId(), jobFactory.createJob(task));

        StringBuilder response = new StringBuilder();
        if (isNewTask) {
            response.append("✅ Новая заявка успешно создана!\n\n");
            response.append("Параметры поиска:\n");
        } else {
            response.append("✅ Заявка успешно обновлена!\n\n");
            response.append("Новые параметры:\n");
        }

        if (task.getKeyword() != null) {
            response.append("🔍 Ключевое слово: ").append(task.getKeyword()).append("\n");
        }

        if (task.getRegionCode() != null) {
            response.append("📍 Регион: ").append(RegionUtil.getRegionName(task.getRegionCode())).append("\n");
        }

        if (task.getMinExperience() != null && task.getMinExperience() > 0) {
            response.append("📅 Опыт: от ").append(task.getMinExperience()).append(" лет\n");
        }

        if (task.getMinSalary() != null && task.getMinSalary() > 0) {
            response.append("💰 Зарплата: от ").append(task.getMinSalary()).append(" руб.\n");
        }

        response.append("\nБот начнёт поиск вакансий по этим критериям каждые 24 часа.");

        if (isNewTask) {
            NewTaskCommand.removeUserTask(message.getUserId());
        } else {
            UpdateTaskCommand.removeEditingTask(message.getUserId());
        }
        KeywordCommand.clearAwaitingFreeText(message.getUserId());

        try {
            vkApiService.sendMessage(message.getUserId(), response.toString(), KeyboardFactory.mainMenu());
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.DONE;
    }
}

