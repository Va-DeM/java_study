package com.vkbot.presentation.command.impl;

import com.vkbot.business.model.SearchTask;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExperienceCommandTest {

    private static final long USER_ID = 505_001L;

    @Mock
    private VKApiService vkApiService;

    private ExperienceCommand command;

    @BeforeEach
    void setUp() {
        command = new ExperienceCommand(vkApiService);
    }

    @AfterEach
    void tearDown() {
        NewTaskCommand.removeUserTask(USER_ID);
    }

    private void seedUserTask() throws Exception {
        Field f = NewTaskCommand.class.getDeclaredField("userTasks");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, SearchTask> map = (Map<Long, SearchTask>) f.get(null);
        map.put(USER_ID, new SearchTask(USER_ID));
    }

    @Test
    void supports_slashCommandBareLabelAndQuickOptions() {
        assertTrue(command.supports("/minExp 2"));
        assertTrue(command.supports("Минимальный опыт"));
        assertTrue(command.supports("Минимальный опыт[2]"));
        assertTrue(command.supports("2 года"));
        assertTrue(!command.supports("что-то ещё"));
    }

    @Test
    void execute_bareLabelShowsOptionsKeyboard() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "Минимальный опыт"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Выберите минимальный опыт"), any());
    }

    @Test
    void execute_slashCommandWithoutArgumentSendsUsageError() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "/minExp"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Пример: /minExp 1"));
    }

    @Test
    void execute_negativeValueIsRejected() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "/minExp -1"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("не может быть отрицательным"));
    }

    @Test
    void execute_nonNumericValueIsRejected() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "/minExp abc"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("корректное значение"));
    }

    @Test
    void execute_quickOptionSetsExperienceOnActiveTask() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "2 года"));

        assertEquals(2, NewTaskCommand.getUserTask(USER_ID).getMinExperience());
    }

    @Test
    void execute_slashCommandSetsExperienceOnActiveTask() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "/minExp 5"));

        assertEquals(5, NewTaskCommand.getUserTask(USER_ID).getMinExperience());
    }

    @Test
    void execute_sendsErrorWhenNoActiveTask() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "/minExp 2"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Сначала создайте"), any());
    }

    @Test
    void getCommandType_isMinExp() {
        assertEquals(CommandType.MIN_EXP, command.getCommandType());
    }
}
