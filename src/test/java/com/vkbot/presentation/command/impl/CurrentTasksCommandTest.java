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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentTasksCommandTest {

    @Mock
    private VKApiService vkApiService;
    @Mock
    private SearchTaskService searchTaskService;
    @Mock
    private UserService userService;

    private CurrentTasksCommand command;

    @BeforeEach
    void setUp() {
        command = new CurrentTasksCommand(vkApiService, searchTaskService, userService);
    }

    @Test
    void supports_matchesButtonLabelAndSlashCommand() {
        assertTrue(command.supports("текущие заявки на поиск"));
        assertTrue(command.supports("/current"));
    }

    @Test
    void execute_sendsErrorWhenNotRegistered() throws Exception {
        when(userService.exists(1L)).thenReturn(false);

        command.execute(new MessageDTO(1L, 1L, "/current"));

        verify(vkApiService).sendMessage(eq(1L), org.mockito.ArgumentMatchers.contains("не зарегистрированы"));
    }

    @Test
    void execute_promptsToCreateWhenNoActiveTasks() throws Exception {
        when(userService.exists(1L)).thenReturn(true);
        when(searchTaskService.findActiveByUserId(1L)).thenReturn(List.of());

        command.execute(new MessageDTO(1L, 1L, "/current"));

        verify(vkApiService).sendMessage(eq(1L), org.mockito.ArgumentMatchers.contains("нет активных"), any());
    }

    @Test
    void execute_listsAllActiveTasksWithDetails() throws Exception {
        when(userService.exists(1L)).thenReturn(true);
        SearchTask task = new SearchTask(1L);
        task.setId(7L);
        task.setKeyword("java");
        task.setRegionCode("77");
        task.setMinExperience(2);
        task.setMinSalary(70000L);
        when(searchTaskService.findActiveByUserId(1L)).thenReturn(List.of(task));

        command.execute(new MessageDTO(1L, 1L, "/current"));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(eq(1L), captor.capture(), any());
        String text = captor.getValue();
        assertTrue(text.contains("Заявка #7"));
        assertTrue(text.contains("java"));
        assertTrue(text.contains("2 лет"));
        assertTrue(text.contains("70000"));
    }

    @Test
    void getCommandType_isCurrentTasks() {
        assertEquals(CommandType.CURRENT_TASKS, command.getCommandType());
    }
}
