package com.vkbot.data.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vkbot.business.model.BotUser;
import com.vkbot.data.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Slf4j
public class FileUserRepository extends AbstractShardedFileRepository<BotUser> implements UserRepository {
    private static final String DIR_NAME = "users";

    public FileUserRepository() {
        super(DIR_NAME, new TypeReference<>() {
        });
    }

    // Только для тестов — хранение во временной директории.
    FileUserRepository(Path baseDir) {
        super(baseDir.resolve(DIR_NAME), new TypeReference<>() {
        });
    }

    @Override
    public Optional<BotUser> findById(Long id) {
        return read(String.valueOf(id), List::stream).findFirst();
    }

    @Override
    public boolean exists(Long id) {
        return read(String.valueOf(id), users -> !users.isEmpty());
    }

    @Override
    public void save(BotUser user) {
        write(String.valueOf(user.getUserId()), users -> {
            users.clear();
            users.add(user);
            log.info("User saved: {}", user.getUserId());
            return null;
        });
    }

    @Override
    public void delete(Long id) {
        write(String.valueOf(id), users -> {
            users.clear();
            log.info("User deleted: {}", id);
            return null;
        });
    }
}
