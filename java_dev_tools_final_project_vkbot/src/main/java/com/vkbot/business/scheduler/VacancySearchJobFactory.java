package com.vkbot.business.scheduler;

import com.vkbot.business.api.VacancyAggregatorApi;
import com.vkbot.business.model.SearchTask;
import com.vkbot.business.model.Vacancy;
import com.vkbot.business.service.NotificationService;
import com.vkbot.business.service.VacancyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class VacancySearchJobFactory {
    private static final int SEARCH_OFFSET = 0;
    private static final int SEARCH_LIMIT = 50;

    private final VacancyAggregatorApi vacancyAggregatorApi;
    private final VacancyService vacancyService;
    private final NotificationService notificationService;

    public Runnable createJob(SearchTask task) {
        return () -> {
            try {
                log.info("Executing scheduled search for user {} (task {})", task.getUserId(), task.getId());
                List<Vacancy> vacancies = vacancyAggregatorApi.searchVacancies(
                        task.getRegionCode(),
                        task.getKeyword(),
                        task.getMinExperience(),
                        task.getMinSalary(),
                        SEARCH_OFFSET,
                        SEARCH_LIMIT
                );

                if (vacancies.isEmpty()) {
                    return;
                }
                vacancies.forEach(v -> v.setTaskId(task.getId()));

                Set<String> alreadyKnownIds = vacancyService.findByTaskId(task.getId()).stream()
                        .map(Vacancy::getId)
                        .collect(Collectors.toSet());
                List<Vacancy> newVacancies = vacancies.stream()
                        .filter(v -> !alreadyKnownIds.contains(v.getId()))
                        .toList();

                if (newVacancies.isEmpty()) {
                    log.info("No new vacancies for user {} (task {})", task.getUserId(), task.getId());
                    return;
                }

                vacancyService.saveAll(newVacancies);
                notificationService.notifyNewVacancies(task, newVacancies);
                log.info("Sent {} new vacancies to user {}", newVacancies.size(), task.getUserId());
            } catch (Exception e) {
                log.error("Error executing scheduled search for task {}", task.getId(), e);
            }
        };
    }
}