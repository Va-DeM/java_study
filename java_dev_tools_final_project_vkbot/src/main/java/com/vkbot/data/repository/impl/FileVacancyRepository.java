package com.vkbot.data.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vkbot.business.model.Vacancy;
import com.vkbot.data.repository.VacancyRepository;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class FileVacancyRepository extends AbstractShardedFileRepository<Vacancy> implements VacancyRepository {
    private static final String DIR_NAME = "vacancies";

    public FileVacancyRepository() {
        super(DIR_NAME, new TypeReference<>() {
        });
    }

    // Только для тестов — хранение во временной директории.
    FileVacancyRepository(Path baseDir) {
        super(baseDir.resolve(DIR_NAME), new TypeReference<>() {
        });
    }

    @Override
    public Optional<Vacancy> findById(String id) {
        return readAll(vacancies -> vacancies.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst());
    }

    @Override
    public List<Vacancy> findByTaskId(Long taskId) {
        return read(String.valueOf(taskId), List::copyOf);
    }

    @Override
    public boolean exists(String id) {
        return readAll(vacancies -> vacancies.stream().anyMatch(v -> v.getId().equals(id)));
    }

    @Override
    public void save(Vacancy vacancy) {
        if (vacancy.getTaskId() == null) {
            throw new IllegalArgumentException("Vacancy.taskId cannot be null");
        }
        write(String.valueOf(vacancy.getTaskId()), vacancies -> {
            vacancies.removeIf(v -> v.getId().equals(vacancy.getId()));
            vacancies.add(vacancy);
            log.info("Vacancy saved: {}", vacancy.getId());
            return null;
        });
    }

    @Override
    public void saveAll(List<Vacancy> newVacancies) {
        for (Vacancy vacancy : newVacancies) {
            if (vacancy.getTaskId() == null) {
                throw new IllegalArgumentException("Vacancy.taskId cannot be null");
            }
        }
        writeAcrossShards(shards -> {
            for (Vacancy vacancy : newVacancies) {
                String key = String.valueOf(vacancy.getTaskId());
                List<Vacancy> taskVacancies = shards.computeIfAbsent(key, k -> new ArrayList<>());
                taskVacancies.removeIf(v -> v.getId().equals(vacancy.getId()));
                taskVacancies.add(vacancy);
            }
            log.info("Saved {} vacancies", newVacancies.size());
            return null;
        });
    }

    @Override
    public void delete(String id) {
        writeAcrossShards(shards -> {
            for (List<Vacancy> vacancies : shards.values()) {
                vacancies.removeIf(v -> v.getId().equals(id));
            }
            log.info("Vacancy deleted: {}", id);
            return null;
        });
    }

    @Override
    public List<Vacancy> findAll() {
        return readAll(List::copyOf);
    }
}
