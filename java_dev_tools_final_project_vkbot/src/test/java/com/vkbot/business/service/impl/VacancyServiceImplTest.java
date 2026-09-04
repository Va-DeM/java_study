package com.vkbot.business.service.impl;

import com.vkbot.business.model.Vacancy;
import com.vkbot.data.repository.VacancyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacancyServiceImplTest {

    @Mock
    private VacancyRepository vacancyRepository;

    private VacancyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VacancyServiceImpl(vacancyRepository);
    }

    @Test
    void findById_delegatesToRepository() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).build();
        when(vacancyRepository.findById("v1")).thenReturn(Optional.of(vacancy));

        assertEquals(Optional.of(vacancy), service.findById("v1"));
    }

    @Test
    void findByTaskId_delegatesToRepository() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).build();
        when(vacancyRepository.findByTaskId(1L)).thenReturn(List.of(vacancy));

        assertEquals(List.of(vacancy), service.findByTaskId(1L));
    }

    @Test
    void save_setsCreatedAtWhenMissing() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).build();

        service.save(vacancy);

        assertNotNull(vacancy.getCreatedAt());
        verify(vacancyRepository).save(vacancy);
    }

    @Test
    void save_keepsExistingCreatedAt() {
        LocalDateTime original = LocalDateTime.of(2020, 1, 1, 0, 0);
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).createdAt(original).build();

        service.save(vacancy);

        assertEquals(original, vacancy.getCreatedAt());
    }

    @Test
    void saveAll_setsCreatedAtOnlyWhenMissing() {
        LocalDateTime original = LocalDateTime.of(2020, 1, 1, 0, 0);
        Vacancy withCreatedAt = Vacancy.builder().id("v1").taskId(1L).createdAt(original).build();
        Vacancy withoutCreatedAt = Vacancy.builder().id("v2").taskId(1L).build();

        service.saveAll(List.of(withCreatedAt, withoutCreatedAt));

        assertEquals(original, withCreatedAt.getCreatedAt());
        assertNotNull(withoutCreatedAt.getCreatedAt());

        ArgumentCaptor<List<Vacancy>> captor = ArgumentCaptor.forClass(List.class);
        verify(vacancyRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void delete_delegatesToRepository() {
        service.delete("v1");

        verify(vacancyRepository).delete("v1");
    }

    @Test
    void filterByMinSalary_keepsVacanciesWithoutSalaryInfo() {
        Vacancy noSalary = Vacancy.builder().id("v1").taskId(1L).build();

        List<Vacancy> result = service.filterByMinSalary(List.of(noSalary), 50000L);

        assertEquals(1, result.size());
    }

    @Test
    void filterByMinSalary_usesSalaryToWhenAvailable() {
        Vacancy belowMin = Vacancy.builder().id("v1").taskId(1L).salaryFrom(10000L).salaryTo(30000L).build();
        Vacancy aboveMin = Vacancy.builder().id("v2").taskId(1L).salaryFrom(10000L).salaryTo(80000L).build();

        List<Vacancy> result = service.filterByMinSalary(List.of(belowMin, aboveMin), 50000L);

        assertEquals(1, result.size());
        assertEquals("v2", result.get(0).getId());
    }

    @Test
    void filterByMinSalary_fallsBackToSalaryFromWhenSalaryToMissing() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).salaryFrom(60000L).build();

        List<Vacancy> result = service.filterByMinSalary(List.of(vacancy), 50000L);

        assertTrue(result.contains(vacancy));
    }
}
