package com.vkbot.business.service.impl;

import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.model.SearchTask;
import com.vkbot.business.model.Vacancy;
import com.vkbot.business.service.VKApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private VKApiService vkApiService;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(vkApiService);
    }

    @Test
    void notifyUser_sendsPlaceholderWhenNoVacancies() throws Exception {
        service.notifyUser(1L, List.of());

        verify(vkApiService).sendMessage(eq(1L), org.mockito.ArgumentMatchers.contains("не найдено"));
    }

    @Test
    void notifyUser_sendsFormattedMessageWhenVacanciesPresent() throws Exception {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).title("Java Developer").build();

        service.notifyUser(1L, List.of(vacancy));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(eq(1L), captor.capture());
        assertTrue(captor.getValue().contains("Java Developer"));
    }

    @Test
    void notifyUser_swallowsClientException() throws Exception {
        doThrow(new ClientException("boom"))
                .when(vkApiService).sendMessage(eq(1L), anyString());

        assertDoesNotThrow(() -> service.notifyUser(1L, List.of()));
    }

    @Test
    void formatVacanciesMessage_capsListAtTenAndSummarizesRemainder() {
        List<Vacancy> vacancies = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            vacancies.add(Vacancy.builder().id("v" + i).taskId(1L).title("Title " + i).build());
        }

        String message = service.formatVacanciesMessage(vacancies);

        assertTrue(message.contains("Найдено 12"));
        assertTrue(message.contains("ещё 2 вакансий"));
    }

    @Test
    void formatVacancyMessage_includesAllOptionalFields() {
        Vacancy vacancy = Vacancy.builder()
                .id("v1")
                .taskId(1L)
                .title("Java Developer")
                .region("Москва")
                .salaryFrom(80000L)
                .salaryTo(120000L)
                .currency("RUR")
                .experienceFrom(3L)
                .url("https://example.com/vacancy")
                .build();

        String message = service.formatVacancyMessage(vacancy);

        assertTrue(message.contains("Java Developer"));
        assertTrue(message.contains("Москва"));
        assertTrue(message.contains("от 80000"));
        assertTrue(message.contains("до 120000"));
        assertTrue(message.contains("RUR"));
        assertTrue(message.contains("3 лет"));
        assertTrue(message.contains("https://example.com/vacancy"));
    }

    @Test
    void formatVacancyMessage_omitsOptionalFieldsWhenAbsent() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).title("Java Developer").build();

        String message = service.formatVacancyMessage(vacancy);

        assertTrue(message.contains("Java Developer"));
        assertTrue(!message.contains("💰"));
        assertTrue(!message.contains("📅"));
        assertTrue(!message.contains("🔗"));
    }

    @Test
    void notifyNewVacancies_doesNothingWhenEmpty() throws Exception {
        SearchTask task = new SearchTask(1L);

        service.notifyNewVacancies(task, List.of());

        verify(vkApiService, never()).sendMessage(org.mockito.ArgumentMatchers.anyLong(), anyString());
    }

    @Test
    void notifyNewVacancies_includesKeywordAndRegionWhenPresent() throws Exception {
        SearchTask task = new SearchTask(1L);
        task.setKeyword("java");
        task.setRegionCode("77");
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).title("Java Developer").build();

        service.notifyNewVacancies(task, List.of(vacancy));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiService).sendMessage(eq(1L), captor.capture());
        assertTrue(captor.getValue().contains("java"));
        assertTrue(captor.getValue().contains("Ключевое слово"));
        assertTrue(captor.getValue().contains("Регион"));
    }
}
