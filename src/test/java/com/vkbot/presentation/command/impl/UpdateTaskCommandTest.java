package com.vkbot.presentation.command.impl;

import com.vkbot.business.model.SearchTask;
import com.vkbot.business.service.SearchTaskService;
import com.vkbot.business.service.UserService;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTaskCommandTest {

    private static final long USER_ID = 502_001L;

    @Mock
    private VKApiService vkApiService;
    @Mock
    private SearchTaskService searchTaskService;
    @Mock
    private UserService userService;

    private UpdateTaskCommand command;

    @BeforeEach
    void setUp() {
        command = new UpdateTaskCommand(vkApiService, searchTaskService, userService);
    }

    @AfterEach
    void tearDown() {
        UpdateTaskCommand.removeEditingTask(USER_ID);
        UpdateTaskCommand.clearPendingSelection(USER_ID);
        KeywordCommand.clearAwaitingFreeText(USER_ID);
    }

    @Test
    void supports_matchesButtonLabelAndSlashCommand() {
        assertTrue(command.supports("обновить задачу на поиск"));
        assertTrue(command.supports("/update"));
    }

    @Test
    void execute_sendsErrorWhenNotRegistered() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(false);

        command.execute(new MessageDTO(USER_ID, USER_ID, "/update"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("не зарегистрированы"));
    }

    @Test
    void execute_sendsErrorWhenNoActiveTasks() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(true);
        when(searchTaskService.findActiveByUserId(USER_ID)).thenReturn(List.of());

        command.execute(new MessageDTO(USER_ID, USER_ID, "/update"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("нет активных"));
    }

    @Test
    void execute_singleTaskGoesStraightToEditingWithDetails() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(true);
        SearchTask task = new SearchTask(USER_ID);
        task.setId(9L);
        task.setKeyword("java");
        when(searchTaskService.findActiveByUserId(USER_ID)).thenReturn(List.of(task));

        command.execute(new MessageDTO(USER_ID, USER_ID, "/update"));

        assertEquals(task, UpdateTaskCommand.getEditingTask(USER_ID));
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(eq(USER_ID), captor.capture(), any());
        assertTrue(captor.getValue().contains("заявки #9"));
        assertTrue(captor.getValue().contains("java"));
    }

    @Test
    void execute_multipleTasksAskForSelectionInsteadOfEditingImmediately() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(true);
        SearchTask first = new SearchTask(USER_ID);
        first.setId(1L);
        SearchTask second = new SearchTask(USER_ID);
        second.setId(2L);
        when(searchTaskService.findActiveByUserId(USER_ID)).thenReturn(List.of(first, second));

        command.execute(new MessageDTO(USER_ID, USER_ID, "/update"));

        assertNull(UpdateTaskCommand.getEditingTask(USER_ID));
        assertEquals(List.of(first, second), UpdateTaskCommand.getPendingSelection(USER_ID));
        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Выберите заявку"), any());
    }

    @Test
    void selectForEditing_setsEditingTaskAndClearsKeywordAwaitFlag() {
        new KeywordCommand(vkApiService).execute(new MessageDTO(USER_ID, USER_ID, "слово для поиска"));
        assertTrue(KeywordCommand.isAwaitingFreeText(USER_ID));

        SearchTask task = new SearchTask(USER_ID);
        task.setId(5L);

        UpdateTaskCommand.selectForEditing(vkApiService, USER_ID, task);

        assertEquals(task, UpdateTaskCommand.getEditingTask(USER_ID));
        assertFalse(KeywordCommand.isAwaitingFreeText(USER_ID));
    }

    @Test
    void getCommandType_isUpdateTask() {
        assertEquals(CommandType.UPDATE_TASK, command.getCommandType());
    }
}
