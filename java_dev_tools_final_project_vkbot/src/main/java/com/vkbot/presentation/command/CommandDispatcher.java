package com.vkbot.presentation.command;

import com.vkbot.presentation.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandDispatcher {
    private final List<BotCommand> commands;

    public CommandDispatcher() {
        this.commands = new ArrayList<>();
    }

    public void register(BotCommand command) {
        commands.add(command);
        log.info("Command registered: {}", command.getCommandType());
    }

    public void registerAll(BotCommand... commandsToRegister) {
        for (BotCommand command : commandsToRegister) {
            register(command);
        }
    }

    public void dispatch(MessageDTO message) {
        if (message == null || message.getText() == null || message.getText().isEmpty()) {
            log.warn("Invalid message received");
            return;
        }

        String text = message.getText().trim();
        log.info("Dispatching message from user {}: {}", message.getUserId(), text);

        for (BotCommand command : commands) {
            if (command.supports(text)) {
                log.info("Executing command: {}", command.getCommandType());
                try {
                    command.execute(message);
                } catch (Exception e) {
                    log.error("Error executing command: {}", command.getCommandType(), e);
                }
                return;
            }
        }

        log.warn("No command found for message: {}", text);
    }

    public BotCommand findCommand(CommandType commandType) {
        return commands.stream()
                .filter(cmd -> cmd.getCommandType() == commandType)
                .findFirst()
                .orElse(null);
    }

    public List<BotCommand> getCommands() {
        return new ArrayList<>(commands);
    }
}

