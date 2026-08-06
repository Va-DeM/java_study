package com.vkbot.business.service;

import com.vkbot.business.model.SearchTask;
import com.vkbot.business.model.Vacancy;

import java.util.List;

public interface NotificationService {
    void notifyUser(Long userId, List<Vacancy> vacancies);

    String formatVacanciesMessage(List<Vacancy> vacancies);

    String formatVacancyMessage(Vacancy vacancy);

    void notifyNewVacancies(SearchTask task, List<Vacancy> vacancies);
}

