package com.vkbot.data.repository.impl;

import com.vkbot.business.model.BotUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUserRepositoryTest {

    @TempDir
    Path tempDir;

    private FileUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FileUserRepository(tempDir);
    }

    @Test
    void save_thenFindById_returnsSameUser() {
        BotUser user = new BotUser(1L, 100L);

        repository.save(user);
        Optional<BotUser> found = repository.findById(1L);

        assertTrue(found.isPresent());
        assertEquals(100L, found.get().getChatId());
    }

    @Test
    void findById_missingUserReturnsEmpty() {
        assertTrue(repository.findById(999L).isEmpty());
    }

    @Test
    void exists_reflectsSaveAndDelete() {
        BotUser user = new BotUser(1L, 100L);

        assertFalse(repository.exists(1L));

        repository.save(user);
        assertTrue(repository.exists(1L));

        repository.delete(1L);
        assertFalse(repository.exists(1L));
    }

    @Test
    void save_overwritesPreviousValueForSameId() {
        BotUser user = new BotUser(1L, 100L);
        repository.save(user);

        user.setChatId(200L);
        repository.save(user);

        assertEquals(200L, repository.findById(1L).get().getChatId());
    }

    @Test
    void save_survivesAcrossRepositoryInstancesUsingSameDirectory() {
        repository.save(new BotUser(1L, 100L));

        FileUserRepository reopened = new FileUserRepository(tempDir);

        assertTrue(reopened.findById(1L).isPresent());
    }
}
