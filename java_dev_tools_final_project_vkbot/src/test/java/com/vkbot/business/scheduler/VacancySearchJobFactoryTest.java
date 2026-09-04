package com.vkbot.business.scheduler;

import com.vkbot.business.api.VacancyAggregatorApi;
import com.vkbot.business.model.SearchTask;
import com.vkbot.business.model.Vacancy;
import com.vkbot.business.service.NotificationService;
import com.vkbot.business.service.VacancyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacancySearchJobFactoryTest {

    @Mock
    private VacancyAggregatorApi vacancyAggregatorApi;
    @Mock
    private VacancyService vacancyService;
    @Mock
    private NotificationService notificationService;

    private VacancySearchJobFactory factory;
    private SearchTask task;

    @BeforeEach
    void setUp() {
        factory = new VacancySearchJobFactory(vacancyAggregatorApi, vacancyService, notificationService);
        task = new SearchTask(1L);
        task.setId(10L);
    }

    @Test
    void createJob_doesNothingWhenNoVacanciesFound() throws Exception {
        when(vacancyAggregatorApi.searchVacancies(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        factory.createJob(task).run();

        verify(vacancyService, never()).saveAll(any());
        verify(notificationService, never()).notifyNewVacancies(any(), any());
    }

    @Test
    void createJob_savesAndNotifiesOnlyNewVacancies() throws Exception {
        Vacancy known = Vacancy.builder().id("v1").build();
        Vacancy fresh = Vacancy.builder().id("v2").build();
        when(vacancyAggregatorApi.searchVacancies(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(known, fresh));
        when(vacancyService.findByTaskId(10L))
                .thenReturn(List.of(Vacancy.builder().id("v1").taskId(10L).build()));

        factory.createJob(task).run();

        verify(vacancyService).saveAll(List.of(fresh));
        verify(notificationService).notifyNewVacancies(task, List.of(fresh));
    }

    @Test
    void createJob_assignsTaskIdToFetchedVacancies() throws Exception {
        Vacancy vacancy = Vacancy.builder().id("v1").build();
        when(vacancyAggregatorApi.searchVacancies(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(vacancy));
        when(vacancyService.findByTaskId(10L)).thenReturn(List.of());

        factory.createJob(task).run();

        org.junit.jupiter.api.Assertions.assertEquals(10L, vacancy.getTaskId());
    }

    @Test
    void createJob_skipsSaveAndNotifyWhenAllVacanciesAlreadyKnown() throws Exception {
        Vacancy known = Vacancy.builder().id("v1").build();
        when(vacancyAggregatorApi.searchVacancies(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(known));
        when(vacancyService.findByTaskId(10L))
                .thenReturn(List.of(Vacancy.builder().id("v1").taskId(10L).build()));

        factory.createJob(task).run();

        verify(vacancyService, never()).saveAll(any());
        verify(notificationService, never()).notifyNewVacancies(any(), any());
    }

    @Test
    void createJob_swallowsExceptionsFromSearch() throws Exception {
        when(vacancyAggregatorApi.searchVacancies(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new com.vkbot.exception.ApiException("boom"));

        assertDoesNotThrow(() -> factory.createJob(task).run());
        verify(vacancyService, never()).saveAll(any());
    }
}
