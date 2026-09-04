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
 * Ловит свободный текст, отправленный сразу после того, как пользователь нажал
 * "Слово для поиска" (см. KeywordCommand.awaitingFreeText) — без префикса /keyword,
 * как показано на скриншотах ТЗ. Должен быть зарегистрирован после всех конкретных
 * команд и до UnknownCommand: если ожидания нет, ведёт себя как обычный fallback.
 */
@Slf4j
@RequiredArgsConstructor
public class KeywordCaptureCommand implements BotCommand {
    private final VKApiService vkApiService;

    @Override
    public boolean supports(String input) {
        return true;
    }

    @Override
    public void execute(MessageDTO message) {
        Long userId = message.getUserId();

        if (!KeywordCommand.isAwaitingFreeText(userId)) {
            try {
                vkApiService.sendMessage(userId, UnknownCommand.FALLBACK_MESSAGE, KeyboardFactory.mainMenu());
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        KeywordCommand.captureFreeText(vkApiService, userId, message.getText());
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.NONE;
    }
}
