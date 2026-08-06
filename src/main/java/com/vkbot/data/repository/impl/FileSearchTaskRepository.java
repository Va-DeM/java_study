package com.vkbot.data.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vkbot.business.model.SearchTask;
import com.vkbot.data.repository.SearchTaskRepository;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class FileSearchTaskRepository extends AbstractShardedFileRepository<SearchTask> implements SearchTaskRepository {
    private static final String DIR_NAME = "search_tasks";

    public FileSearchTaskRepository() {
        super(DIR_NAME, new TypeReference<>() {
        });
    }

    // Только для тестов — хранение во временной директории.
    FileSearchTaskRepository(Path baseDir) {
        super(baseDir.resolve(DIR_NAME), new TypeReference<>() {
        });
    }

    @Override
    public Optional<SearchTask> findById(Long id) {
        return readAll(tasks -> tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst());
    }

    @Override
    public List<SearchTask> findByUserId(Long userId) {
        return read(String.valueOf(userId), List::copyOf);
    }

    @Override
    public boolean exists(Long id) {
        return readAll(tasks -> tasks.stream().anyMatch(t -> t.getId().equals(id)));
    }

    @Override
    public void save(SearchTask task) {
        writeAcrossShards(shards -> {
            String key = String.valueOf(task.getUserId());
            List<SearchTask> userTasks = shards.computeIfAbsent(key, k -> new ArrayList<>());
            if (task.getId() == null) {
                long nextId = shards.values().stream()
                        .flatMap(List::stream)
                        .mapToLong(SearchTask::getId)
                        .max().orElse(0L) + 1;
                task.setId(nextId);
            } else {
                userTasks.removeIf(t -> t.getId().equals(task.getId()));
            }
            userTasks.add(task);
            log.info("Search task saved: {}", task.getId());
            return null;
        });
    }

    @Override
    public void delete(Long id) {
        writeAcrossShards(shards -> {
            for (List<SearchTask> tasks : shards.values()) {
                tasks.removeIf(t -> t.getId().equals(id));
            }
            log.info("Search task deleted: {}", id);
            return null;
        });
    }

    @Override
    public List<SearchTask> findAll() {
        return readAll(List::copyOf);
    }

    @Override
    public List<SearchTask> findAllActive() {
        return readAll(tasks -> tasks.stream()
                .filter(SearchTask::isActive)
                .toList());
    }
}
