package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.model.SearchTask;
import com.vkbot.business.scheduler.TaskScheduler;
import com.vkbot.business.service.SearchTaskService;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Обрабатывает голое число, которое пользователь отправляет в ответ на вопрос
 * "какую из нескольких активных заявок выбрать" (см. DeleteTaskCommand/UpdateTaskCommand).
 * Матчит только сам факт "это положительное целое число" — конфликтов с другими
 * командами нет, так как кнопки опыта/зарплаты используют точные наборы строк,
 * не голые числа (см. ExperienceCommand/SalaryCommand). Если ожидания выбора нет —
 * отвечает тем же fallback-сообщением, что и UnknownCommand.
 */
@Slf4j
@RequiredArgsConstructor
public class TaskSelectionCommand implements BotCommand {
    private final VKApiService vkApiService;
    private final SearchTaskService searchTaskService;
    private final TaskScheduler taskScheduler;

    @Override
    public boolean supports(String input) {
        return input.trim().matches("\\d+");
    }

    @Override
    public void execute(MessageDTO message) {
        Long userId = message.getUserId();
        int index = Integer.parseInt(message.getText().trim());
        log.info("Executing TaskSelectionCommand for user: {}, index: {}", userId, index);

        List<SearchTask> deletionCandidates = DeleteTaskCommand.getPendingDeletion(userId);
        if (deletionCandidates != null) {
            DeleteTaskCommand.clearPendingDeletion(userId);
            SearchTask task = pick(deletionCandidates, index);
            if (task == null) {
                sendInvalidIndex(userId, deletionCandidates.size());
                return;
            }
            DeleteTaskCommand.performDelete(vkApiService, searchTaskService, taskScheduler, userId, task);
            return;
        }

        List<SearchTask> updateCandidates = UpdateTaskCommand.getPendingSelection(userId);
        if (updateCandidates != null) {
            UpdateTaskCommand.clearPendingSelection(userId);
            SearchTask task = pick(updateCandidates, index);
            if (task == null) {
                sendInvalidIndex(userId, updateCandidates.size());
                return;
            }
            UpdateTaskCommand.selectForEditing(vkApiService, userId, task);
            return;
        }

        // Числовое "ключевое слово" (например "2024"), введённое свободным текстом
        // после клика "Слово для поиска" — иначе оно ошибочно воспринялось бы как номер заявки.
        if (KeywordCommand.isAwaitingFreeText(userId)) {
            KeywordCommand.captureFreeText(vkApiService, userId, message.getText());
            return;
        }

        try {
            vkApiService.sendMessage(userId, UnknownCommand.FALLBACK_MESSAGE, KeyboardFactory.mainMenu());
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    private SearchTask pick(List<SearchTask> tasks, int index) {
        if (index < 1 || index > tasks.size()) {
            return null;
        }
        return tasks.get(index - 1);
    }

    private void sendInvalidIndex(Long userId, int max) {
        try {
            vkApiService.sendMessage(userId, "❌ Нет заявки с таким номером. Отправьте число от 1 до " + max + ".");
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.NONE;
    }
}
