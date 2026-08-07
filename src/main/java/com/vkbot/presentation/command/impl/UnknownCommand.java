package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Обработчик по умолчанию — срабатывает, только если ни одна другая команда не подошла.
 * Должен быть зарегистрирован в CommandDispatcher последним.
 */
@Slf4j
@RequiredArgsConstructor
public class UnknownCommand implements BotCommand {
    public static final String FALLBACK_MESSAGE =
            "🤔 Не понимаю эту команду. Выберите действие на клавиатуре или введите /menu.";

    private final VKApiService vkApiService;

    @Override
    public boolean supports(String input) {
        return true;
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing UnknownCommand (fallback) for user: {}, text: '{}'",
                message.getUserId(), message.getText());
        try {
            vkApiService.sendMessage(message.getUserId(), FALLBACK_MESSAGE, KeyboardFactory.mainMenu());
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.NONE;
    }
}
