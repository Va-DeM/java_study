package com.vkbot.data.repository;

import com.vkbot.business.model.BotUser;

import java.util.Optional;

public interface UserRepository {
    Optional<BotUser> findById(Long id);

    boolean exists(Long id);

    void save(BotUser user);

    void delete(Long id);
}

