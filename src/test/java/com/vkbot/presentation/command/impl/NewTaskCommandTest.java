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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewTaskCommandTest {

    private static final long USER_ID = 501_001L;

    @Mock
    private VKApiService vkApiService;
    @Mock
    private SearchTaskService searchTaskService;
    @Mock
    private UserService userService;

    private NewTaskCommand command;

    @BeforeEach
    void setUp() {
        command = new NewTaskCommand(vkApiService, searchTaskService, userService);
    }

    @AfterEach
    void tearDown() {
        NewTaskCommand.removeUserTask(USER_ID);
    }

    @Test
    void supports_matchesButtonLabelAndSlashCommand() {
        assertTrue(command.supports("создать заявку на поиск вакансий"));
        assertTrue(command.supports("/newTask"));
    }

    @Test
    void execute_sendsErrorWhenUserNotRegistered() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(false);

        command.execute(new MessageDTO(USER_ID, USER_ID, "/newTask"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("не зарегистрированы"));
        assertNull(NewTaskCommand.getUserTask(USER_ID));
    }

    @Test
    void execute_startsNewTaskSessionForRegisteredUser() throws Exception {
        when(userService.exists(USER_ID)).thenReturn(true);

        command.execute(new MessageDTO(USER_ID, USER_ID, "/newTask"));

        SearchTask task = NewTaskCommand.getUserTask(USER_ID);
        assertEquals(USER_ID, task.getUserId());
        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.anyString(), any());
    }

    @Test
    void getCommandType_isNewTask() {
        assertEquals(CommandType.NEW_TASK, command.getCommandType());
    }
}
