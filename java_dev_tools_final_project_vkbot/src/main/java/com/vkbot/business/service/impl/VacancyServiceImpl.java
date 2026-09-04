package com.vkbot.business.service.impl;

import com.vkbot.business.model.Vacancy;
import com.vkbot.business.service.VacancyService;
import com.vkbot.data.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {
    private final VacancyRepository vacancyRepository;

    @Override
    public Optional<Vacancy> findById(String id) {
        log.info("Finding vacancy by ID: {}", id);
        return vacancyRepository.findById(id);
    }

    @Override
    public List<Vacancy> findByTaskId(Long taskId) {
        log.info("Finding vacancies by task ID: {}", taskId);
        return vacancyRepository.findByTaskId(taskId);
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        log.info("Saving vacancy: {}", vacancy.getId());
        if (vacancy.getCreatedAt() == null) {
            vacancy.setCreatedAt(LocalDateTime.now());
        }
        vacancyRepository.save(vacancy);
        return vacancy;
    }

    @Override
    public void saveAll(List<Vacancy> vacancies) {
        log.info("Saving {} vacancies", vacancies.size());
        vacancies.forEach(v -> {
            if (v.getCreatedAt() == null) {
                v.setCreatedAt(LocalDateTime.now());
            }
        });
        vacancyRepository.saveAll(vacancies);
    }

    @Override
    public void delete(String id) {
        log.info("Deleting vacancy: {}", id);
        vacancyRepository.delete(id);
    }

    @Override
    public List<Vacancy> filterByMinSalary(List<Vacancy> vacancies, Long minSalary) {
        log.info("Filtering {} vacancies by minimum salary: {}", vacancies.size(), minSalary);
        return vacancies.stream()
                .filter(v -> {
                    if (v.getSalaryTo() == null && v.getSalaryFrom() == null) {
                        return true;
                    }
                    Long maxSalary = v.getSalaryTo() != null ? v.getSalaryTo() : v.getSalaryFrom();
                    return maxSalary != null && maxSalary >= minSalary;
                })
                .toList();
    }
}

