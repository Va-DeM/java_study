package com.vkbot.business.service.impl;

import com.vkbot.business.model.BotUser;
import com.vkbot.data.repository.UserRepository;
import com.vkbot.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void findById_delegatesToRepository() {
        BotUser user = new BotUser(1L, 2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<BotUser> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void exists_delegatesToRepository() {
        when(userRepository.exists(1L)).thenReturn(true);

        assertTrue(userService.exists(1L));
    }

    @Test
    void save_setsCreatedAtWhenMissing() {
        BotUser user = new BotUser(1L, 2L);
        assertNull(user.getCreatedAt());

        userService.save(user);

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        verify(userRepository).save(user);
    }

    @Test
    void save_keepsExistingCreatedAt() {
        BotUser user = new BotUser(1L, 2L);
        LocalDateTime originalCreatedAt = LocalDateTime.of(2020, 1, 1, 0, 0);
        user.setCreatedAt(originalCreatedAt);

        userService.save(user);

        assertEquals(originalCreatedAt, user.getCreatedAt());
    }

    @Test
    void save_alwaysRefreshesUpdatedAt() {
        BotUser user = new BotUser(1L, 2L);
        user.setUpdatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));

        userService.save(user);

        assertTrue(user.getUpdatedAt().isAfter(LocalDateTime.of(2020, 1, 1, 0, 0)));
    }

    @Test
    void delete_delegatesToRepositoryWhenUserExists() {
        when(userRepository.exists(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).delete(1L);
    }

    @Test
    void delete_throwsWhenUserDoesNotExist() {
        when(userRepository.exists(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.delete(1L));
        verify(userRepository, never()).delete(1L);
    }

    @Test
    void getOrCreate_returnsExistingUserWithoutSaving() {
        BotUser existing = new BotUser(1L, 2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        BotUser result = userService.getOrCreate(1L, 2L);

        assertEquals(existing, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getOrCreate_createsAndSavesNewUserWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BotUser result = userService.getOrCreate(1L, 99L);

        assertEquals(1L, result.getUserId());
        assertEquals(99L, result.getChatId());
        assertTrue(result.isActive());
        verify(userRepository, times(1)).save(eq(result));
    }
}
