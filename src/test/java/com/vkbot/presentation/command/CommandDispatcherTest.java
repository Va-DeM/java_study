package com.vkbot.presentation.command;

import com.vkbot.presentation.dto.MessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {

    @Mock
    private BotCommand firstCommand;
    @Mock
    private BotCommand secondCommand;

    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new CommandDispatcher();
    }

    @Test
    void registerAll_addsAllCommands() {
        dispatcher.registerAll(firstCommand, secondCommand);

        assertEquals(2, dispatcher.getCommands().size());
    }

    @Test
    void getCommands_returnsDefensiveCopy() {
        dispatcher.register(firstCommand);

        dispatcher.getCommands().clear();

        assertEquals(1, dispatcher.getCommands().size());
    }

    @Test
    void dispatch_nullMessageDoesNothing() {
        dispatcher.register(firstCommand);

        assertDoesNotThrow(() -> dispatcher.dispatch(null));

        verify(firstCommand, never()).execute(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void dispatch_blankTextDoesNothing() {
        dispatcher.register(firstCommand);
        MessageDTO message = new MessageDTO(1L, 1L, "   ");

        dispatcher.dispatch(message);

        verify(firstCommand, never()).execute(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void dispatch_executesFirstMatchingCommandOnly() {
        when(firstCommand.supports(anyString())).thenReturn(true);
        when(firstCommand.getCommandType()).thenReturn(CommandType.NONE);
        dispatcher.registerAll(firstCommand, secondCommand);
        MessageDTO message = new MessageDTO(1L, 1L, "/start");

        dispatcher.dispatch(message);

        verify(firstCommand, times(1)).execute(message);
        verify(secondCommand, never()).supports(anyString());
        verify(secondCommand, never()).execute(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void dispatch_skipsNonMatchingCommandsAndUsesLaterMatch() {
        when(firstCommand.supports(anyString())).thenReturn(false);
        when(secondCommand.supports(anyString())).thenReturn(true);
        when(secondCommand.getCommandType()).thenReturn(CommandType.NONE);
        dispatcher.registerAll(firstCommand, secondCommand);
        MessageDTO message = new MessageDTO(1L, 1L, "hello");

        dispatcher.dispatch(message);

        verify(firstCommand, never()).execute(org.mockito.ArgumentMatchers.any());
        verify(secondCommand).execute(message);
    }

    @Test
    void dispatch_noMatchingCommandDoesNotThrow() {
        when(firstCommand.supports(anyString())).thenReturn(false);
        dispatcher.register(firstCommand);
        MessageDTO message = new MessageDTO(1L, 1L, "unrecognized");

        assertDoesNotThrow(() -> dispatcher.dispatch(message));
    }

    @Test
    void dispatch_swallowsExceptionThrownByCommand() {
        when(firstCommand.supports(anyString())).thenReturn(true);
        when(firstCommand.getCommandType()).thenReturn(CommandType.NONE);
        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(firstCommand).execute(org.mockito.ArgumentMatchers.any());
        dispatcher.register(firstCommand);
        MessageDTO message = new MessageDTO(1L, 1L, "/start");

        assertDoesNotThrow(() -> dispatcher.dispatch(message));
    }

    @Test
    void findCommand_returnsMatchByType() {
        when(firstCommand.getCommandType()).thenReturn(CommandType.DEFAULT);
        dispatcher.register(firstCommand);

        assertEquals(firstCommand, dispatcher.findCommand(CommandType.DEFAULT));
    }

    @Test
    void findCommand_returnsNullWhenNoMatch() {
        when(firstCommand.getCommandType()).thenReturn(CommandType.DEFAULT);
        dispatcher.register(firstCommand);

        assertNull(dispatcher.findCommand(CommandType.DONE));
    }
}
