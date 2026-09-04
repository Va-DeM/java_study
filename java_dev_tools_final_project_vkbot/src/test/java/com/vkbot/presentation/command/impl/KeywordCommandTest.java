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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KeywordCommandTest {

    private static final long USER_ID = 507_001L;

    @Mock
    private VKApiService vkApiService;

    private KeywordCommand command;

    @BeforeEach
    void setUp() {
        command = new KeywordCommand(vkApiService);
    }

    @AfterEach
    void tearDown() {
        NewTaskCommand.removeUserTask(USER_ID);
        KeywordCommand.clearAwaitingFreeText(USER_ID);
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
        assertTrue(command.supports("/keyword Java"));
        assertTrue(command.supports("Слово для поиска"));
        assertTrue(command.supports("Слово для поиска[java]"));
        assertTrue(command.supports("Java"));
        assertTrue(!command.supports("что-то ещё"));
    }

    @Test
    void execute_bareLabelShowsOptionsAndSetsAwaitingFlag() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "Слово для поиска"));

        assertTrue(KeywordCommand.isAwaitingFreeText(USER_ID));
        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.anyString(), any());
    }

    @Test
    void execute_slashCommandWithoutArgumentSendsUsageError() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "/keyword"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Пример: /keyword Java"));
    }

    @Test
    void execute_quickOptionSetsKeywordAndClearsAwaitingFlag() throws Exception {
        seedUserTask();
        command.execute(new MessageDTO(USER_ID, USER_ID, "Слово для поиска"));
        assertTrue(KeywordCommand.isAwaitingFreeText(USER_ID));

        command.execute(new MessageDTO(USER_ID, USER_ID, "Java"));

        assertEquals("Java", NewTaskCommand.getUserTask(USER_ID).getKeyword());
        assertFalse(KeywordCommand.isAwaitingFreeText(USER_ID));
    }

    @Test
    void execute_slashCommandSetsKeywordOnActiveTask() throws Exception {
        seedUserTask();

        command.execute(new MessageDTO(USER_ID, USER_ID, "/keyword java разработчик"));

        assertEquals("java разработчик", NewTaskCommand.getUserTask(USER_ID).getKeyword());
    }

    @Test
    void execute_sendsErrorWhenNoActiveTask() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "/keyword Java"));

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("Сначала создайте"), any());
    }

    @Test
    void captureFreeText_setsKeywordDirectlyWithoutSlashPrefix() throws Exception {
        seedUserTask();
        KeywordCommand.captureFreeText(vkApiService, USER_ID, "java разработчик");

        assertEquals("java разработчик", NewTaskCommand.getUserTask(USER_ID).getKeyword());
        assertFalse(KeywordCommand.isAwaitingFreeText(USER_ID));
    }

    @Test
    void captureFreeText_rejectsBlankInput() throws Exception {
        KeywordCommand.captureFreeText(vkApiService, USER_ID, "   ");

        verify(vkApiService).sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("не может быть пустым"));
    }

    @Test
    void getCommandType_isKeyword() {
        assertEquals(CommandType.KEYWORD, command.getCommandType());
    }
}
