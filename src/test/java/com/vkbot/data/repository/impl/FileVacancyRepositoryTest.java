package com.vkbot.data.repository.impl;

import com.vkbot.business.model.Vacancy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileVacancyRepositoryTest {

    @TempDir
    Path tempDir;

    private FileVacancyRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FileVacancyRepository(tempDir);
    }

    @Test
    void save_thenFindByTaskId_returnsSavedVacancy() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(10L).title("Java Dev").build();

        repository.save(vacancy);

        assertEquals(1, repository.findByTaskId(10L).size());
    }

    @Test
    void save_requiresNonNullTaskId() {
        Vacancy vacancy = Vacancy.builder().id("v1").build();

        assertThrows(IllegalArgumentException.class, () -> repository.save(vacancy));
    }

    @Test
    void save_upsertsById() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(10L).title("Java Dev").build();
        repository.save(vacancy);

        Vacancy updated = Vacancy.builder().id("v1").taskId(10L).title("Senior Java Dev").build();
        repository.save(updated);

        List<Vacancy> stored = repository.findByTaskId(10L);
        assertEquals(1, stored.size());
        assertEquals("Senior Java Dev", stored.get(0).getTitle());
    }

    @Test
    void saveAll_requiresNonNullTaskIdForEveryItem() {
        Vacancy valid = Vacancy.builder().id("v1").taskId(10L).build();
        Vacancy invalid = Vacancy.builder().id("v2").build();

        assertThrows(IllegalArgumentException.class, () -> repository.saveAll(List.of(valid, invalid)));
    }

    @Test
    void saveAll_groupsVacanciesIntoDifferentTaskShards() {
        Vacancy forTask1 = Vacancy.builder().id("v1").taskId(1L).build();
        Vacancy forTask2 = Vacancy.builder().id("v2").taskId(2L).build();

        repository.saveAll(List.of(forTask1, forTask2));

        assertEquals(1, repository.findByTaskId(1L).size());
        assertEquals(1, repository.findByTaskId(2L).size());
    }

    @Test
    void findById_scansAcrossAllShards() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(10L).build();
        repository.save(vacancy);

        assertTrue(repository.findById("v1").isPresent());
    }

    @Test
    void delete_removesFromWhicheverShardHoldsIt() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(10L).build();
        repository.save(vacancy);

        repository.delete("v1");

        assertTrue(repository.findById("v1").isEmpty());
        assertTrue(repository.findByTaskId(10L).isEmpty());
    }

    @Test
    void findAll_returnsEverythingAcrossShards() {
        repository.save(Vacancy.builder().id("v1").taskId(1L).build());
        repository.save(Vacancy.builder().id("v2").taskId(2L).build());

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void exists_reflectsPresenceAcrossShards() {
        repository.save(Vacancy.builder().id("v1").taskId(1L).build());

        assertTrue(repository.exists("v1"));
        assertTrue(!repository.exists("missing"));
    }
}
