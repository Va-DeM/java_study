package com.vkbot.business.service;

import com.vkbot.business.model.BotUser;

import java.util.Optional;

public interface UserService {
    Optional<BotUser> findById(Long userId);

    boolean exists(Long userId);

    BotUser save(BotUser user);

    void delete(Long userId);

    BotUser getOrCreate(Long userId, Long chatId);
}

