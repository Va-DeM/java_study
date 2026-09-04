package com.vkbot.data.repository;

import com.vkbot.business.model.Vacancy;

import java.util.List;
import java.util.Optional;

public interface VacancyRepository {
    Optional<Vacancy> findById(String id);

    List<Vacancy> findByTaskId(Long taskId);

    boolean exists(String id);

    void save(Vacancy vacancy);

    void saveAll(List<Vacancy> vacancies);

    void delete(String id);

    List<Vacancy> findAll();
}

