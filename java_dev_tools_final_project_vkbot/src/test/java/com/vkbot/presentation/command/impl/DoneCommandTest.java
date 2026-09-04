package com.vkbot.presentation.command.impl;

import com.vkbot.business.model.SearchTask;
import com.vkbot.business.scheduler.TaskScheduler;
import com.vkbot.business.scheduler.VacancySearchJobFactory;
import com.vkbot.business.service.SearchTaskService;
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

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoneCommandTest {

    private static final long USER_ID = 509_001L;

    @Mock
    private VKApiService vkApiService;
    @Mock
    private SearchTaskService searchTaskService;
    @Mock
    private TaskScheduler taskScheduler;
    @Mock
    private VacancySearchJobFactory jobFactory;
    @Mock
    private Runnable job;

    private DoneCommand command;

    @BeforeEach
    void setUp() {
        command = new DoneCommand(vkApiService, searchTaskService, taskScheduler, jobFactory);
    }

    @AfterEach
    void tearDown() {
        NewTaskCommand.removeUserTask(USER_ID);
        UpdateTaskCommand.removeEditingTask(USER_ID);
    }

    private void seedNewTask(SearchTask task) throws Exception {
        Field f = NewTaskCommand.class.getDeclaredField("userTasks");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, SearchTask> map = (Map<Long, SearchTask>) f.get(null);
        map.put(USER_ID, task);
    }

    @Test
    void supports_matchesButtonLabelAndSlashCommand() {
        assertTrue(command.supports("готово"));
        assertTrue(command.supports("/done"));
    }

    @Test
    void execute_sendsErrorWhenNoActiveSession() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "/done"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Нет активной заявки"));
    }

    @Test
    void execute_savesNewTaskAndSchedulesIt() throws Exception {
        SearchTask task = new SearchTask(USER_ID);
        task.setId(15L);
        task.setKeyword("java");
        task.setRegionCode("77");
        task.setMinExperience(2);
        task.setMinSalary(70000L);
        seedNewTask(task);
        when(jobFactory.createJob(task)).thenReturn(job);

        command.execute(new MessageDTO(USER_ID, USER_ID, "/done"));

        verify(searchTaskService).save(task);
        verify(taskScheduler).scheduleTask(15L, job);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(eq(USER_ID), captor.capture(), any());
        assertTrue(captor.getValue().contains("Новая заявка успешно создана"));
        assertTrue(captor.getValue().contains("java"));
        org.junit.jupiter.api.Assertions.assertNull(NewTaskCommand.getUserTask(USER_ID));
    }

    @Test
    void execute_savingUpdatedTaskUsesUpdateWording() throws Exception {
        SearchTask task = new SearchTask(USER_ID);
        task.setId(16L);
        UpdateTaskCommand.selectForEditing(vkApiService, USER_ID, task);
        when(jobFactory.createJob(task)).thenReturn(job);

        command.execute(new MessageDTO(USER_ID, USER_ID, "/done"));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService, org.mockito.Mockito.atLeastOnce()).sendMessage(eq(USER_ID), captor.capture(), any());
        assertTrue(captor.getAllValues().stream().anyMatch(m -> m.contains("Заявка успешно обновлена")));
        org.junit.jupiter.api.Assertions.assertNull(UpdateTaskCommand.getEditingTask(USER_ID));
    }

    @Test
    void getCommandType_isDone() {
        assertEquals(CommandType.DONE, command.getCommandType());
    }
}
