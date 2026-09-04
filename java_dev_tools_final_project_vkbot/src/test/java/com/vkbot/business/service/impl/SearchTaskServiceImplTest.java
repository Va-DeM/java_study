package com.vkbot.business.service.impl;

import com.vkbot.business.model.SearchTask;
import com.vkbot.data.repository.SearchTaskRepository;
import com.vkbot.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchTaskServiceImplTest {

    @Mock
    private SearchTaskRepository searchTaskRepository;

    private SearchTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SearchTaskServiceImpl(searchTaskRepository);
    }

    @Test
    void findById_delegatesToRepository() {
        SearchTask task = new SearchTask(1L);
        when(searchTaskRepository.findById(5L)).thenReturn(Optional.of(task));

        assertEquals(Optional.of(task), service.findById(5L));
    }

    @Test
    void findByUserId_delegatesToRepository() {
        SearchTask task = new SearchTask(1L);
        when(searchTaskRepository.findByUserId(1L)).thenReturn(List.of(task));

        assertEquals(List.of(task), service.findByUserId(1L));
    }

    @Test
    void save_setsCreatedAtWhenMissing() {
        SearchTask task = new SearchTask(1L);

        service.save(task);

        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
        verify(searchTaskRepository).save(task);
    }

    @Test
    void save_keepsExistingCreatedAt() {
        SearchTask task = new SearchTask(1L);
        LocalDateTime original = LocalDateTime.of(2020, 1, 1, 0, 0);
        task.setCreatedAt(original);

        service.save(task);

        assertEquals(original, task.getCreatedAt());
    }

    @Test
    void delete_delegatesToRepositoryWhenTaskExists() {
        when(searchTaskRepository.exists(7L)).thenReturn(true);

        service.delete(7L);

        verify(searchTaskRepository).delete(7L);
    }

    @Test
    void delete_throwsWhenTaskDoesNotExist() {
        when(searchTaskRepository.exists(7L)).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> service.delete(7L));
        verify(searchTaskRepository, never()).delete(7L);
    }

    @Test
    void findAllActive_delegatesToRepository() {
        SearchTask task = new SearchTask(1L);
        task.setActive(true);
        when(searchTaskRepository.findAllActive()).thenReturn(List.of(task));

        assertEquals(List.of(task), service.findAllActive());
    }

    @Test
    void findActiveByUserId_filtersOutInactiveTasks() {
        SearchTask active = new SearchTask(1L);
        active.setActive(true);
        SearchTask inactive = new SearchTask(1L);
        inactive.setActive(false);
        when(searchTaskRepository.findByUserId(1L)).thenReturn(List.of(active, inactive));

        List<SearchTask> result = service.findActiveByUserId(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }
}
