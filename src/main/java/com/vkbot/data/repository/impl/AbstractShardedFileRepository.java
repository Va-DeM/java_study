package com.vkbot.data.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vkbot.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Файловое хранилище, шардированное по ключу владельца (ID пользователя, ID задачи и т.п.) —
 * один JSON-файл на ключ, вместо одного общего файла на всю коллекцию.
 */
@Slf4j
public abstract class AbstractShardedFileRepository<T> {
    private static final String DATA_DIR = "data";

    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Path baseDir;
    private final TypeReference<List<T>> typeReference;

    protected AbstractShardedFileRepository(String dirName, TypeReference<List<T>> typeReference) {
        this(Path.of(DATA_DIR, dirName), typeReference);
    }

    // Пакетно-видимый конструктор — только для тестов, чтобы направить хранилище
    // во временную директорию вместо реального "data/" в рабочем дереве проекта.
    AbstractShardedFileRepository(Path baseDir, TypeReference<List<T>> typeReference) {
        this.baseDir = baseDir;
        this.typeReference = typeReference;
        try {
            Files.createDirectories(baseDir);
            log.info("Sharded file storage initialized: {}", baseDir);
        } catch (IOException e) {
            log.error("Error initializing file storage directory: {}", baseDir, e);
        }
    }

    protected <R> R read(String key, Function<List<T>, R> reader) {
        lock.readLock().lock();
        try {
            return reader.apply(loadShard(key));
        } finally {
            lock.readLock().unlock();
        }
    }

    protected <R> R write(String key, Function<List<T>, R> mutator) {
        lock.writeLock().lock();
        try {
            List<T> items = loadShard(key);
            R result = mutator.apply(items);
            saveShard(key, items);
            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected <R> R readAll(Function<List<T>, R> reader) {
        lock.readLock().lock();
        try {
            List<T> all = new ArrayList<>();
            for (String key : listShardKeys()) {
                all.addAll(loadShard(key));
            }
            return reader.apply(all);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Загружает все шарды в изменяемую Map(ключ -> список), передаёт её в mutator,
     * затем персистит все шарды обратно (пустой список удаляет файл шарда).
     * Нужно для операций, затрагивающих несколько владельцев сразу (генерация id, поиск/удаление по id сущности).
     */
    protected <R> R writeAcrossShards(Function<Map<String, List<T>>, R> mutator) {
        lock.writeLock().lock();
        try {
            Map<String, List<T>> shards = new HashMap<>();
            for (String key : listShardKeys()) {
                shards.put(key, loadShard(key));
            }
            R result = mutator.apply(shards);
            for (Map.Entry<String, List<T>> entry : shards.entrySet()) {
                saveShard(entry.getKey(), entry.getValue());
            }
            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<String> listShardKeys() {
        File[] files = baseDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return List.of();
        }
        List<String> keys = new ArrayList<>();
        for (File file : files) {
            String name = file.getName();
            keys.add(name.substring(0, name.length() - ".json".length()));
        }
        return keys;
    }

    private List<T> loadShard(String key) {
        File file = shardFile(key);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(file, typeReference);
        } catch (IOException e) {
            log.error("Error reading shard: {}", file, e);
            return new ArrayList<>();
        }
    }

    private void saveShard(String key, List<T> items) {
        File file = shardFile(key);
        try {
            if (items.isEmpty()) {
                Files.deleteIfExists(file.toPath());
                return;
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, items);
        } catch (IOException e) {
            log.error("Error writing shard: {}", file, e);
        }
    }

    private File shardFile(String key) {
        return baseDir.resolve(key + ".json").toFile();
    }
}
