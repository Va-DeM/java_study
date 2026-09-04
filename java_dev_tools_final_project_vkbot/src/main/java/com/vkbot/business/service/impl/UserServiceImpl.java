package com.vkbot.business.service.impl;

import com.vkbot.business.model.BotUser;
import com.vkbot.business.service.UserService;
import com.vkbot.data.repository.UserRepository;
import com.vkbot.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Optional<BotUser> findById(Long userId) {
        log.info("Finding user by ID: {}", userId);
        return userRepository.findById(userId);
    }

    @Override
    public boolean exists(Long userId) {
        return userRepository.exists(userId);
    }

    @Override
    public BotUser save(BotUser user) {
        log.info("Saving user: {}", user.getUserId());
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    @Override
    public void delete(Long userId) {
        log.info("Deleting user: {}", userId);
        if (!userRepository.exists(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        userRepository.delete(userId);
    }

    @Override
    public BotUser getOrCreate(Long userId, Long chatId) {
        log.info("Getting or creating user: {}", userId);
        Optional<BotUser> existingUser = userRepository.findById(userId);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        BotUser newUser = new BotUser(userId, chatId);
        save(newUser);
        return newUser;
    }
}

