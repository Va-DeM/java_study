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
class SalaryCommandTest {

    private static final long USER_ID = 506_001L;

    @Mock
    private VKApiService vkApiService;

    private SalaryCommand command;

    @BeforeEach
    void setUp() {
        command = new SalaryCommand(vkApiService);
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
        assertTrue(command.supports("/minSalary 70000"));
        assertTrue(command.supports("Минимальная зарплата"));
        assertTrue(command.supports("Минимальная зарплата[70000]"));
        assertTrue(command.supports("70000"));
        assertTrue(!command.supports("что-то ещё"));
    }

    @Test
    void supports_doesNotClaimSmallBareNumbers() {
        assertTrue(!command.supports("1"));
        assertTrue(!command.supports("2"));
    }

    @Test
    void execute_bareLabelShowsOptionsKeyboard() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "Минимальная зарплата"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Выберите минимальную зарплату"), any());
    }

    @Test
    void execute_negativeValueIsRejected() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "/minSalary -1"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("не может быть отрицательной"));
    }

    @Test
    void execute_quickOptionSetsSalaryOnActiveTask() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "70000"));

        assertEquals(70000L, NewTaskCommand.getUserTask(USER_ID).getMinSalary());
    }

    @Test
    void execute_slashCommandSetsSalaryOnActiveTask() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "/minSalary 50000"));

        assertEquals(50000L, NewTaskCommand.getUserTask(USER_ID).getMinSalary());
    }

    @Test
    void execute_sendsErrorWhenNoActiveTask() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "/minSalary 50000"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Сначала создайте"), any());
    }

    @Test
    void getCommandType_isMinSalary() {
        assertEquals(CommandType.MIN_SALARY, command.getCommandType());
    }
}
