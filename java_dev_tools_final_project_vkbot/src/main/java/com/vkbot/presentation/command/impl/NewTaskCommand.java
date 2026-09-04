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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class NewTaskCommand implements BotCommand {
    private final VKApiService vkApiService;
    private final SearchTaskService searchTaskService;
    private final UserService userService;

    private static final Map<Long, SearchTask> userTasks = new HashMap<>();

    @Override
    public boolean supports(String input) {
        return input.equalsIgnoreCase("создать заявку на поиск вакансий") ||
               input.equalsIgnoreCase("/newTask");
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing NewTaskCommand for user: {}", message.getUserId());

        if (!userService.exists(message.getUserId())) {
            try {
                vkApiService.sendMessage(message.getUserId(),
                    "❌ Вы не зарегистрированы. Используйте команду 'Начать' для регистрации.");
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        SearchTask newTask = new SearchTask(message.getUserId());
        userTasks.put(message.getUserId(), newTask);
        KeywordCommand.clearAwaitingFreeText(message.getUserId());

        String response = """
                ✏️ Начинаем создание новой заявки на поиск вакансий!

                Выберите параметр из списка ниже (все поля необязательные) или задайте его
                текстовой командой: /region [код], /minExp [лет], /minSalary [сумма], /keyword [слово].

                Нажмите "Готово" (/done), когда закончите.""";

        try {
            vkApiService.sendMessage(message.getUserId(), response, KeyboardFactory.taskEditMenu(newTask));
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.NEW_TASK;
    }

    public static SearchTask getUserTask(Long userId) {
        return userTasks.get(userId);
    }

    public static void removeUserTask(Long userId) {
        userTasks.remove(userId);
    }
}

