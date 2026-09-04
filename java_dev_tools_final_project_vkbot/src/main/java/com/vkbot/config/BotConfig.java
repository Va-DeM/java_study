package com.vkbot.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
@Getter
public class BotConfig {
    private final String vkAccessToken;
    private final Long groupId;
    private final String apiVersion;

    public BotConfig() {
        this(System::getenv);
    }

    // Пакетно-видимый конструктор — только для тестов, чтобы подставить фиктивные
    // переменные окружения вместо System.getenv().
    BotConfig(Function<String, String> env) {
        this.vkAccessToken = loadEnvironmentVariable(env, "VK_BOT_API");
        this.groupId = loadGroupId(env);
        this.apiVersion = "5.199";

        if (vkAccessToken == null || vkAccessToken.isEmpty()) {
            throw new IllegalArgumentException("VK_BOT_API environment variable is not set");
        }
        if (groupId == null || groupId <= 0) {
            throw new IllegalArgumentException("VK_GROUP_ID environment variable is not set or invalid");
        }

        log.info("BotConfig initialized successfully");
        log.info("Group ID: {}", groupId);
        log.info("API Version: {}", apiVersion);
    }

    private String loadEnvironmentVariable(Function<String, String> env, String varName) {
        String value = env.apply(varName);
        if (value == null || value.isEmpty()) {
            log.warn("Environment variable {} is not set", varName);
            return null;
        }
        return value;
    }

    private Long loadGroupId(Function<String, String> env) {
        String groupIdStr = env.apply("VK_GROUP_ID");
        if (groupIdStr == null || groupIdStr.isEmpty()) {
            log.error("VK_GROUP_ID environment variable is not set");
            return null;
        }
        try {
            return Long.parseLong(groupIdStr);
        } catch (NumberFormatException e) {
            log.error("Invalid VK_GROUP_ID value: {}", groupIdStr);
            return null;
        }
    }


    @Override
    public String toString() {
        return "BotConfig{" +
                "groupId=" + groupId +
                ", apiVersion='" + apiVersion + '\'' +
                '}';
    }
}
