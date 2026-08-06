package com.vkbot.business.service;

import com.vkbot.business.model.SearchTask;

import java.util.List;
import java.util.Optional;

public interface SearchTaskService {
    Optional<SearchTask> findById(Long id);

    List<SearchTask> findByUserId(Long userId);

    SearchTask save(SearchTask task);

    void delete(Long id);

    List<SearchTask> findAllActive();

    List<SearchTask> findActiveByUserId(Long userId);
}

