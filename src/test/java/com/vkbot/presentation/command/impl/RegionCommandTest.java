package com.vkbot.presentation.command.impl;

import com.vkbot.business.model.SearchTask;
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

@ExtendWith(MockitoExtension.class)
class RegionCommandTest {

    private static final long USER_ID = 504_001L;

    @Mock
    private VKApiService vkApiService;

    private RegionCommand command;

    @BeforeEach
    void setUp() {
        command = new RegionCommand(vkApiService);
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
    void supports_slashCommandBareLabelAndButtonClick() {
        assertTrue(command.supports("/region 10"));
        assertTrue(command.supports("Регион"));
        assertTrue(command.supports("Регион[10]"));
        assertTrue(command.supports("Республика Карелия, 10"));
        assertTrue(!command.supports("что-то ещё"));
    }

    @Test
    void execute_bareLabelReopensRegionPicker() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "Регион"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("страница 1"), any());
    }

    @Test
    void execute_slashCommandWithoutArgumentSendsUsageError() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "/region"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Пример: /region 10"));
    }

    @Test
    void execute_slashCommandWithUnknownCodeSendsError() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "/region 999"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("не найден"));
    }

    @Test
    void execute_slashCommandNormalizesLeadingZero() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "/region 05"));

        assertEquals("5", NewTaskCommand.getUserTask(USER_ID).getRegionCode());
    }

    @Test
    void execute_buttonClickSetsRegionAndReturnsTaskEditMenu() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "Республика Карелия, 10"));

        assertEquals("10", NewTaskCommand.getUserTask(USER_ID).getRegionCode());
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(eq(USER_ID), captor.capture(), any());
        assertTrue(captor.getValue().contains("Республика Карелия"));
    }

    @Test
    void execute_sendsErrorWhenNoActiveTaskToApplyRegionTo() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "/region 10"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Сначала создайте"));
    }

    @Test
    void getCommandType_isRegion() {
        assertEquals(CommandType.REGION, command.getCommandType());
    }
}
