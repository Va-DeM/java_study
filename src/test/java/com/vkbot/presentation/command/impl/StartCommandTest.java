package com.vkbot.presentation.command.impl;

import com.vkbot.business.model.BotUser;
import com.vkbot.business.service.UserService;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {

    @Mock
    private UserService userService;
    @Mock
    private VKApiService vkApiService;

    private StartCommand command;

    @BeforeEach
    void setUp() {
        command = new StartCommand(userService, vkApiService);
    }

    @Test
    void supports_matchesStartWordAndSlashCommand() {
        assertTrue(command.supports("начать"));
        assertTrue(command.supports("Начать"));
        assertTrue(command.supports("/start"));
        assertTrue(!command.supports("привет"));
    }

    @Test
    void execute_registersUserAndSendsWelcomeWithKeyboard() throws Exception {
        MessageDTO message = new MessageDTO(1L, 100L, "начать");

        command.execute(message);

        verify(userService).getOrCreate(1L, 100L);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(org.mockito.ArgumentMatchers.eq(1L), textCaptor.capture(),
                org.mockito.ArgumentMatchers.any());
        assertTrue(textCaptor.getValue().contains("Добро пожаловать"));
    }

    @Test
    void getCommandType_isDefault() {
        assertEquals(CommandType.DEFAULT, command.getCommandType());
    }
}
