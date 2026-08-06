package com.vkbot.presentation.command.impl;

import com.vkbot.business.model.SearchTask;
import com.vkbot.business.service.SearchTaskService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MainMenuCommandTest {

    @Mock
    private VKApiService vkApiService;
    @Mock
    private SearchTaskService searchTaskService;
    @Mock
    private UserService userService;

    private MainMenuCommand command;

    @BeforeEach
    void setUp() {
        command = new MainMenuCommand(vkApiService, searchTaskService, userService);
    }

    @Test
    void supports_matchesMenuWordAndSlashCommand() {
        assertTrue(command.supports("показать меню"));
        assertTrue(command.supports("/menu"));
        assertTrue(!command.supports("что-то другое"));
    }

    @Test
    void execute_sendsNotRegisteredErrorWhenUserMissing() throws Exception {
        when(userService.exists(1L)).thenReturn(false);

        command.execute(new MessageDTO(1L, 1L, "/menu"));

        verify(vkApiService).sendMessage(eq(1L), org.mockito.ArgumentMatchers.contains("не зарегистрированы"));
        verify(searchTaskService, never()).findActiveByUserId(any());
    }

    @Test
    void execute_showsTaskCountWhenActiveTasksExist() throws Exception {
        when(userService.exists(1L)).thenReturn(true);
        when(searchTaskService.findActiveByUserId(1L)).thenReturn(List.of(new SearchTask(1L)));

        command.execute(new MessageDTO(1L, 1L, "/menu"));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(eq(1L), captor.capture(), any());
        assertTrue(captor.getValue().contains("(1)"));
    }

    @Test
    void execute_showsNoActiveTasksWhenEmpty() throws Exception {
        when(userService.exists(1L)).thenReturn(true);
        when(searchTaskService.findActiveByUserId(1L)).thenReturn(List.of());

        command.execute(new MessageDTO(1L, 1L, "/menu"));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(eq(1L), captor.capture(), any());
        assertTrue(captor.getValue().contains("нет активных"));
    }

    @Test
    void getCommandType_isMainMenu() {
        assertEquals(CommandType.MAIN_MENU, command.getCommandType());
    }
}
