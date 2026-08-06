package com.vkbot.presentation.command.impl;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NextCommandTest {

    private static final long USER_ID = 510_001L;

    @Mock
    private VKApiService vkApiService;

    private NextCommand command;

    @BeforeEach
    void setUp() {
        command = new NextCommand(vkApiService);
    }

    @AfterEach
    void tearDown() {
        // Сбросить курсор страницы на первую, чтобы не утекало в другие тесты.
        NextCommand.showFirstRegionPage(vkApiService, USER_ID);
    }

    @Test
    void supports_matchesButtonLabelAndSlashCommand() {
        assertTrue(command.supports("Далее"));
        assertTrue(command.supports("/next"));
    }

    @Test
    void showFirstRegionPage_startsAtPageOneWithFirstFiveRegions() throws Exception {
        NextCommand.showFirstRegionPage(vkApiService, USER_ID);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(eq(USER_ID), captor.capture(), any());
        assertTrue(captor.getValue().contains("страница 1"));
        assertTrue(captor.getValue().contains("Республика Адыгея"));
    }

    @Test
    void execute_advancesToSecondPage() throws Exception {
        NextCommand.showFirstRegionPage(vkApiService, USER_ID);

        command.execute(new MessageDTO(USER_ID, USER_ID, "Далее"));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService, org.mockito.Mockito.atLeastOnce()).sendMessage(eq(USER_ID), captor.capture(), any());
        assertTrue(captor.getValue().contains("страница 2"));
        assertTrue(captor.getValue().contains("Республика Ингушетия"));
    }

    @Test
    void execute_onLastPageSendsNoMorePagesMessage() throws Exception {
        NextCommand.showFirstRegionPage(vkApiService, USER_ID);
        for (int i = 0; i < 20; i++) {
            command.execute(new MessageDTO(USER_ID, USER_ID, "Далее"));
        }

        verify(vkApiService, org.mockito.Mockito.atLeastOnce())
                .sendMessage(eq(USER_ID), org.mockito.ArgumentMatchers.contains("последней странице"));
    }

    @Test
    void getCommandType_isNext() {
        assertEquals(CommandType.NEXT, command.getCommandType());
    }
}
