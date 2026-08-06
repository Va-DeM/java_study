package com.vkbot.business.service.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.model.SearchTask;
import com.vkbot.business.model.Vacancy;
import com.vkbot.business.service.NotificationService;
import com.vkbot.business.service.VKApiService;
import com.vkbot.util.RegionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final VKApiService vkApiService;

    @Override
    public void notifyUser(Long userId, List<Vacancy> vacancies) {
        log.info("Notifying user {} about {} vacancies", userId, vacancies.size());

        if (vacancies.isEmpty()) {
            try {
                vkApiService.sendMessage(userId, "😔 К сожалению, по вашим критериям новых вакансий не найдено.");
            } catch (ApiException | ClientException e) {
                log.error("Error sending notification to user {}", userId, e);
            }
            return;
        }

        String message = formatVacanciesMessage(vacancies);
        try {
            vkApiService.sendMessage(userId, message);
        } catch (ApiException | ClientException e) {
            log.error("Error sending notification to user {}", userId, e);
        }
    }

    @Override
    public String formatVacanciesMessage(List<Vacancy> vacancies) {
        StringBuilder message = new StringBuilder();
        message.append("🎉 Найдено ").append(vacancies.size()).append(" новых вакансий:\n\n");

        for (int i = 0; i < Math.min(vacancies.size(), 10); i++) {
            message.append(formatVacancyMessage(vacancies.get(i)));
            if (i < vacancies.size() - 1) {
                message.append("\n");
            }
        }

        if (vacancies.size() > 10) {
            message.append("\n\n... и ещё ").append(vacancies.size() - 10).append(" вакансий");
        }

        return message.toString();
    }

    @Override
    public String formatVacancyMessage(Vacancy vacancy) {
        StringBuilder message = new StringBuilder();
        message.append("💼 ").append(vacancy.getTitle()).append("\n");

        if (vacancy.getRegion() != null) {
            message.append("📍 ").append(vacancy.getRegion()).append("\n");
        }

        if (vacancy.getSalaryFrom() != null || vacancy.getSalaryTo() != null) {
            message.append("💰 ");
            if (vacancy.getSalaryFrom() != null) {
                message.append("от ").append(vacancy.getSalaryFrom());
            }
            if (vacancy.getSalaryTo() != null) {
                message.append(" до ").append(vacancy.getSalaryTo());
            }
            if (vacancy.getCurrency() != null) {
                message.append(" ").append(vacancy.getCurrency());
            }
            message.append("\n");
        }

        if (vacancy.getExperienceFrom() != null) {
            message.append("📅 Опыт: ").append(vacancy.getExperienceFrom()).append(" лет\n");
        }

        if (vacancy.getUrl() != null) {
            message.append("🔗 ").append(vacancy.getUrl()).append("\n");
        }

        return message.toString();
    }

    @Override
    public void notifyNewVacancies(SearchTask task, List<Vacancy> vacancies) {
        log.info("Notifying user {} about {} new vacancies for task {}", task.getUserId(), vacancies.size(), task.getId());

        if (vacancies.isEmpty()) {
            log.debug("No new vacancies found for task {}", task.getId());
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("🎯 По вашему поиску найдены новые вакансии!\n");

        if (task.getKeyword() != null) {
            message.append("Ключевое слово: ").append(task.getKeyword()).append("\n");
        }

        if (task.getRegionCode() != null) {
            message.append("Регион: ").append(RegionUtil.getRegionName(task.getRegionCode())).append("\n");
        }

        message.append("\n").append(formatVacanciesMessage(vacancies));

        try {
            vkApiService.sendMessage(task.getUserId(), message.toString());
        } catch (ApiException | ClientException e) {
            log.error("Error sending notification to user {}", task.getUserId(), e);
        }
    }
}

