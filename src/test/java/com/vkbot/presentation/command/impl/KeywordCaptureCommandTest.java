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
class KeywordCaptureCommandTest {

    private static final long USER_ID = 508_001L;

    @Mock
    private VKApiService vkApiService;

    private KeywordCaptureCommand command;

    @BeforeEach
    void setUp() {
        command = new KeywordCaptureCommand(vkApiService);
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
    void supports_alwaysReturnsTrue() {
        assertTrue(command.supports("любой текст"));
        assertTrue(command.supports(""));
    }

    @Test
    void execute_fallsBackToUnknownMessageWhenNothingIsPending() throws Exception {
        command.execute(new MessageDTO(USER_ID, USER_ID, "случайный текст"));

        verify(vkApiService).sendMessage(eq(USER_ID), eq(UnknownCommand.FALLBACK_MESSAGE), any());
    }

    @Test
    void execute_capturesFreeTextAsKeywordWhenAwaiting() throws Exception {
        seedUserTask();
        new KeywordCommand(vkApiService).execute(new MessageDTO(USER_ID, USER_ID, "слово для поиска"));

        command.execute(new MessageDTO(USER_ID, USER_ID, "java разработчик"));

        assertEquals("java разработчик", NewTaskCommand.getUserTask(USER_ID).getKeyword());
    }

    @Test
    void getCommandType_isNone() {
        assertEquals(CommandType.NONE, command.getCommandType());
    }
}
