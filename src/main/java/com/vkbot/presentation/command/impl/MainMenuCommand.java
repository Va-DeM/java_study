package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.service.SearchTaskService;
import com.vkbot.business.service.UserService;
import com.vkbot.business.service.impl.SearchTaskServiceImpl;
import com.vkbot.business.service.impl.UserServiceImpl;
import com.vkbot.business.service.impl.VKApiServiceImpl;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MainMenuCommand implements BotCommand {
    private final VKApiService vkApiService;
    private final SearchTaskService searchTaskService;
    private final UserService userService;

    @Override
    public boolean supports(String input) {
        return input.equalsIgnoreCase("показать меню") || input.equalsIgnoreCase("/menu");
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing MainMenuCommand for user: {}", message.getUserId());

        if (!userService.exists(message.getUserId())) {
            try {
                vkApiService.sendMessage(message.getUserId(),
                    "❌ Вы не зарегистрированы. Используйте команду 'Начать' для регистрации.");
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        var activeTasks = searchTaskService.findActiveByUserId(message.getUserId());

        StringBuilder menu = new StringBuilder();
        menu.append("\n📋 Главное меню\n\n");
        menu.append("Выберите действие:\n");
        menu.append("1️⃣ Создать заявку на поиск вакансий\n");
        menu.append("2️⃣ Текущие заявки на поиск");

        if (activeTasks.isEmpty()) {
            menu.append(" (нет активных)");
        } else {
            menu.append(" (").append(activeTasks.size()).append(")");
        }

        menu.append("\n3️⃣ Обновить задачу на поиск\n");
        menu.append("4️⃣ Удалить заявку на поиск\n\n");
        menu.append("Отправьте сообщение с текстом для выполнения действия.");

        try {
            vkApiService.sendMessage(message.getUserId(), menu.toString(), KeyboardFactory.mainMenu());
        } catch (ApiException | ClientException e) {
            log.error("Error sending menu", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.MAIN_MENU;
    }
}

