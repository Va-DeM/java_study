package com.vkbot.presentation.command;

import com.vkbot.presentation.dto.MessageDTO;

public interface BotCommand {
    boolean supports(String input);

    void execute(MessageDTO message);

    CommandType getCommandType();
}

