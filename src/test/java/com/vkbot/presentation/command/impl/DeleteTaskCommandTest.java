package com.vkbot.presentation.command.impl;

import com.vkbot.business.model.SearchTask;
import com.vkbot.business.scheduler.TaskScheduler;
import com.vkbot.business.service.SearchTaskService;
import com.vkbot.business.service.UserService;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class DeleteTaskCommandTest {

    private static final long USER_ID = 503_001L;

    @Mock
    private VKApiService vkApiService;
    @Mock
    private SearchTaskService searchTaskService;
    @Mock
    private UserService userService;
    @Mock
    private TaskScheduler taskScheduler;

    private DeleteTaskCommand command;

    @BeforeEach
    void setUp() {
        command = new DeleteTaskCommand(vkApiService, searchTaskService, userService, taskScheduler);
    }

    @AfterEach
    void tearDown() {
        DeleteTaskCommand.clearPendingDeletion(USER_ID);
    }

    @Test
    void supports_matchesButtonLabelAndStopCommand() {
        assertTrue(command.supports("удалить заявку на поиск"));
        assertTrue(command.supports("/stop"));
    }

    @Test
    void execute_sendsErrorWhenNotRegistered() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(false);

        command.execute(new MessageDTO(USER_ID, USER_ID, "/stop"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("не зарегистрированы"));
    }

    @Test
    void execute_sendsErrorWhenNoActiveTasks() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(true);
        when(searchTaskService.findActiveByUserId(USER_ID)).thenReturn(List.of());

        command.execute(new MessageDTO(USER_ID, USER_ID, "/stop"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("нет активных"));
    }

    @Test
    void execute_singleTaskIsDeletedImmediately() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(true);
        SearchTask task = new SearchTask(USER_ID);
        task.setId(11L);
        when(searchTaskService.findActiveByUserId(USER_ID)).thenReturn(List.of(task));

        command.execute(new MessageDTO(USER_ID, USER_ID, "/stop"));

        assertFalse(task.isActive());
        verify(searchTaskService).delete(11L);
        verify(taskScheduler).cancelTask(11L);
        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("успешно удалена"), any());
    }

    @Test
    void execute_multipleTasksAskForSelection() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(true);
        SearchTask first = new SearchTask(USER_ID);
        first.setId(1L);
        SearchTask second = new SearchTask(USER_ID);
        second.setId(2L);
        when(searchTaskService.findActiveByUserId(USER_ID)).thenReturn(List.of(first, second));

        command.execute(new MessageDTO(USER_ID, USER_ID, "/stop"));

        assertEquals(List.of(first, second), DeleteTaskCommand.getPendingDeletion(USER_ID));
        verify(searchTaskService, org.mockito.Mockito.never()).delete(org.mockito.ArgumentMatchers.anyLong());
        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Выберите заявку"), any());
    }

    @Test
    void performDelete_isStaticallyReusableByTaskSelectionCommand() {
        SearchTask task = new SearchTask(USER_ID);
        task.setId(21L);

        DeleteTaskCommand.performDelete(vkApiService, searchTaskService, taskScheduler, USER_ID, task);

        verify(searchTaskService).delete(21L);
        verify(taskScheduler).cancelTask(21L);
    }

    @Test
    void getPendingDeletion_defaultsToNullWhenNothingPending() {
        assertNull(DeleteTaskCommand.getPendingDeletion(999_999L));
    }

    @Test
    void getCommandType_isDeleteTask() {
        assertEquals(CommandType.DELETE_TASK, command.getCommandType());
    }
}
