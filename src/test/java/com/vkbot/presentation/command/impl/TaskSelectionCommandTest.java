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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskSelectionCommandTest {

    private static final long USER_ID = 511_001L;

    @Mock
    private VKApiService vkApiService;
    @Mock
    private SearchTaskService searchTaskService;
    @Mock
    private UserService userService;
    @Mock
    private TaskScheduler taskScheduler;

    private TaskSelectionCommand command;

    @BeforeEach
    void setUp() {
        command = new TaskSelectionCommand(vkApiService, searchTaskService, taskScheduler);
        lenient().when(userService.exists(USER_ID)).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        DeleteTaskCommand.clearPendingDeletion(USER_ID);
        UpdateTaskCommand.clearPendingSelection(USER_ID);
        KeywordCommand.clearAwaitingFreeText(USER_ID);
        NewTaskCommand.removeUserTask(USER_ID);
        UpdateTaskCommand.removeEditingTask(USER_ID);
    }

    @Test
    void supports_onlyPlainPositiveIntegers() {
        assertTrue(command.supports("1"));
        assertTrue(command.supports("42"));
        assertTrue(!command.supports("1 год"));
        assertTrue(!command.supports("-1"));
        assertTrue(!command.supports("java"));
    }

    @Test
    void execute_resolvesPendingDeletionByIndex() {
        SearchTask first = new SearchTask(USER_ID);
        first.setId(1L);
        SearchTask second = new SearchTask(USER_ID);
        second.setId(2L);
        when(searchTaskService.findActiveByUserId(USER_ID)).thenReturn(List.of(first, second));
        new DeleteTaskCommand(vkApiService, searchTaskService, userService, taskScheduler)
                .execute(new MessageDTO(USER_ID, USER_ID, "/stop"));

        command.execute(new MessageDTO(USER_ID, USER_ID, "2"));

        verify(searchTaskService).delete(2L);
        verify(taskScheduler).cancelTask(2L);
    }

    @Test
    void execute_invalidDeletionIndexAsksAgainWithoutClearingSilently() throws Exception {
        SearchTask first = new SearchTask(USER_ID);
        first.setId(1L);
        SearchTask second = new SearchTask(USER_ID);
        second.setId(2L);
        when(searchTaskService.findActiveByUserId(USER_ID)).thenReturn(List.of(first, second));
        new DeleteTaskCommand(vkApiService, searchTaskService, userService, taskScheduler)
                .execute(new MessageDTO(USER_ID, USER_ID, "/stop"));

        command.execute(new MessageDTO(USER_ID, USER_ID, "99"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Нет заявки с таким номером"));
        verify(searchTaskService, org.mockito.Mockito.never()).delete(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void execute_resolvesPendingUpdateSelectionByIndex() {
        SearchTask first = new SearchTask(USER_ID);
        first.setId(1L);
        SearchTask second = new SearchTask(USER_ID);
        second.setId(2L);
        when(searchTaskService.findActiveByUserId(USER_ID)).thenReturn(List.of(first, second));
        new UpdateTaskCommand(vkApiService, searchTaskService, userService)
                .execute(new MessageDTO(USER_ID, USER_ID, "/update"));

        command.execute(new MessageDTO(USER_ID, USER_ID, "1"));

        assertEquals(first, UpdateTaskCommand.getEditingTask(USER_ID));
    }

    @Test
    void execute_capturesNumericKeywordInsteadOfTreatingItAsSelection() {
        java.lang.reflect.Field f;
        try {
            f = NewTaskCommand.class.getDeclaredField("userTasks");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Long, SearchTask> map = (java.util.Map<Long, SearchTask>) f.get(null);
            map.put(USER_ID, new SearchTask(USER_ID));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        new KeywordCommand(vkApiService).execute(new MessageDTO(USER_ID, USER_ID, "слово для поиска"));

        command.execute(new MessageDTO(USER_ID, USER_ID, "2024"));

        assertEquals("2024", NewTaskCommand.getUserTask(USER_ID).getKeyword());
    }

    @Test
    void execute_fallsBackToGenericMessageWhenNothingIsPending() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "7"));

        verify(vkApiService).sendMessage(eq(USER_ID), eq(UnknownCommand.FALLBACK_MESSAGE), any());
    }

    @Test
    void getCommandType_isNone() {
        assertEquals(CommandType.NONE, command.getCommandType());
    }
}
