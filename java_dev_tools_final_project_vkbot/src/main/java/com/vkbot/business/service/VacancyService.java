package com.vkbot.business.service;

import com.vkbot.business.model.Vacancy;

import java.util.List;
import java.util.Optional;

public interface VacancyService {
    Optional<Vacancy> findById(String id);

    List<Vacancy> findByTaskId(Long taskId);

    Vacancy save(Vacancy vacancy);

    void saveAll(List<Vacancy> vacancies);

    void delete(String id);

    List<Vacancy> filterByMinSalary(List<Vacancy> vacancies, Long minSalary);
}

