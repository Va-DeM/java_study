package com.vkbot.business.service.impl;

import com.vkbot.business.model.SearchTask;
import com.vkbot.business.service.SearchTaskService;
import com.vkbot.data.repository.SearchTaskRepository;
import com.vkbot.exception.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class SearchTaskServiceImpl implements SearchTaskService {
    private final SearchTaskRepository searchTaskRepository;

    @Override
    public Optional<SearchTask> findById(Long id) {
        log.info("Finding search task by ID: {}", id);
        return searchTaskRepository.findById(id);
    }

    @Override
    public List<SearchTask> findByUserId(Long userId) {
        log.info("Finding search tasks by user ID: {}", userId);
        return searchTaskRepository.findByUserId(userId);
    }

    @Override
    public SearchTask save(SearchTask task) {
        log.info("Saving search task for user: {}", task.getUserId());
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }
        task.setUpdatedAt(LocalDateTime.now());
        searchTaskRepository.save(task);
        return task;
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting search task: {}", id);
        if (!searchTaskRepository.exists(id)) {
            throw new TaskNotFoundException("Search task not found: " + id);
        }
        searchTaskRepository.delete(id);
    }

    @Override
    public List<SearchTask> findAllActive() {
        log.info("Finding all active search tasks");
        return searchTaskRepository.findAllActive();
    }

    @Override
    public List<SearchTask> findActiveByUserId(Long userId) {
        log.info("Finding active search tasks by user ID: {}", userId);
        return searchTaskRepository.findByUserId(userId).stream()
                .filter(SearchTask::isActive)
                .toList();
    }
}

