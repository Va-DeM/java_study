package com.vkbot.data.repository.impl;

import com.vkbot.business.model.SearchTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSearchTaskRepositoryTest {

    @TempDir
    Path tempDir;

    private FileSearchTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FileSearchTaskRepository(tempDir);
    }

    @Test
    void save_assignsGeneratedIdOnInsert() {
        SearchTask task = new SearchTask(1L);

        repository.save(task);

        assertNotNull(task.getId());
    }

    @Test
    void save_generatesDistinctIdsAcrossDifferentUserShards() {
        SearchTask taskA = new SearchTask(1L);
        SearchTask taskB = new SearchTask(2L);

        repository.save(taskA);
        repository.save(taskB);

        assertTrue(!taskA.getId().equals(taskB.getId()));
    }

    @Test
    void save_updatesExistingTaskInPlace() {
        SearchTask task = new SearchTask(1L);
        repository.save(task);
        Long id = task.getId();

        task.setKeyword("java");
        repository.save(task);

        assertEquals("java", repository.findById(id).get().getKeyword());
        assertEquals(1, repository.findByUserId(1L).size());
    }

    @Test
    void findByUserId_isScopedToASingleShard() {
        repository.save(new SearchTask(1L));
        repository.save(new SearchTask(1L));
        repository.save(new SearchTask(2L));

        assertEquals(2, repository.findByUserId(1L).size());
        assertEquals(1, repository.findByUserId(2L).size());
    }

    @Test
    void findById_scansAcrossAllShards() {
        SearchTask task = new SearchTask(42L);
        repository.save(task);

        Optional<SearchTask> found = repository.findById(task.getId());

        assertTrue(found.isPresent());
        assertEquals(42L, found.get().getUserId());
    }

    @Test
    void delete_removesTaskFromItsOwnerShardOnly() {
        SearchTask a = new SearchTask(1L);
        SearchTask b = new SearchTask(1L);
        repository.save(a);
        repository.save(b);

        repository.delete(a.getId());

        assertTrue(repository.findById(a.getId()).isEmpty());
        assertEquals(1, repository.findByUserId(1L).size());
    }

    @Test
    void findAllActive_filtersOutInactiveAcrossShards() {
        SearchTask active = new SearchTask(1L);
        active.setActive(true);
        SearchTask inactive = new SearchTask(2L);
        inactive.setActive(false);
        repository.save(active);
        repository.save(inactive);

        List<SearchTask> result = repository.findAllActive();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    void findAll_returnsEverythingAcrossShards() {
        repository.save(new SearchTask(1L));
        repository.save(new SearchTask(2L));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void exists_reflectsPresenceAcrossShards() {
        SearchTask task = new SearchTask(1L);
        repository.save(task);

        assertTrue(repository.exists(task.getId()));
        assertTrue(!repository.exists(999999L));
    }
}
