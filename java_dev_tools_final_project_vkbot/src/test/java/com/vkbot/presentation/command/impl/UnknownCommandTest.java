package com.vkbot.presentation.command.impl;

import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnknownCommandTest {

    @Mock
    private VKApiService vkApiService;

    private UnknownCommand command;

    @BeforeEach
    void setUp() {
        command = new UnknownCommand(vkApiService);
    }

    @Test
    void supports_matchesAnyInput() {
        assertTrue(command.supports("что угодно"));
        assertTrue(command.supports(""));
        assertTrue(command.supports("12345"));
    }

    @Test
    void execute_sendsFallbackMessageWithMainMenuKeyboard() throws Exception {
        command.execute(new MessageDTO(1L, 1L, "абракадабра"));

        verify(vkApiService).sendMessage(eq(1L), eq(UnknownCommand.FALLBACK_MESSAGE), any());
    }

    @Test
    void getCommandType_isNone() {
        assertEquals(CommandType.NONE, command.getCommandType());
    }
}
