package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.service.UserService;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class StartCommand implements BotCommand {
    private final UserService userService;
    private final VKApiService vkApiService;

    @Override
    public boolean supports(String input) {
        return input.equalsIgnoreCase("начать") || input.equalsIgnoreCase("/start");
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing StartCommand for user: {}", message.getUserId());

        userService.getOrCreate(message.getUserId(), message.getChatId());

        String welcomeMessage = """
                👋 Добро пожаловать в ВК-бот по поиску вакансий!

                Я помогу вам находить подходящие вакансии и получать уведомления о новых предложениях.

                Используйте команду "Показать меню" или /menu для начала работы.""";

        try {
            vkApiService.sendMessage(message.getUserId(), welcomeMessage, KeyboardFactory.mainMenu());
        } catch (ApiException | ClientException e) {
            log.error("Error sending welcome message", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.DEFAULT;
    }
}

