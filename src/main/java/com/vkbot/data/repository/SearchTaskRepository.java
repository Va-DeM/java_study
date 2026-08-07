package com.vkbot.data.repository;

import com.vkbot.business.model.SearchTask;

import java.util.List;
import java.util.Optional;

public interface SearchTaskRepository {
    Optional<SearchTask> findById(Long id);

    List<SearchTask> findByUserId(Long userId);

    boolean exists(Long id);

    void save(SearchTask task);

    void delete(Long id);

    List<SearchTask> findAll();

    List<SearchTask> findAllActive();
}

