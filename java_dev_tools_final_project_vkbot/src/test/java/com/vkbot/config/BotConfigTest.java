package com.vkbot.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BotConfigTest {

    @Test
    void constructor_loadsValidEnvironment() {
        Map<String, String> env = Map.of("VK_BOT_API", "token123", "VK_GROUP_ID", "42");

        BotConfig config = new BotConfig(env::get);

        assertEquals("token123", config.getVkAccessToken());
        assertEquals(42L, config.getGroupId());
        assertEquals("5.199", config.getApiVersion());
    }

    @Test
    void constructor_throwsWhenTokenMissing() {
        Map<String, String> env = Map.of("VK_GROUP_ID", "42");

        assertThrows(IllegalArgumentException.class, () -> new BotConfig(env::get));
    }

    @Test
    void constructor_throwsWhenTokenBlank() {
        Map<String, String> env = Map.of("VK_BOT_API", "", "VK_GROUP_ID", "42");

        assertThrows(IllegalArgumentException.class, () -> new BotConfig(env::get));
    }

    @Test
    void constructor_throwsWhenGroupIdMissing() {
        Map<String, String> env = Map.of("VK_BOT_API", "token123");

        assertThrows(IllegalArgumentException.class, () -> new BotConfig(env::get));
    }

    @Test
    void constructor_throwsWhenGroupIdNotNumeric() {
        Map<String, String> env = Map.of("VK_BOT_API", "token123", "VK_GROUP_ID", "not-a-number");

        assertThrows(IllegalArgumentException.class, () -> new BotConfig(env::get));
    }

    @Test
    void constructor_throwsWhenGroupIdZeroOrNegative() {
        Map<String, String> env = Map.of("VK_BOT_API", "token123", "VK_GROUP_ID", "0");

        assertThrows(IllegalArgumentException.class, () -> new BotConfig(env::get));
    }

    @Test
    void toString_includesGroupIdAndApiVersionButNotToken() {
        Map<String, String> env = Map.of("VK_BOT_API", "secret-token", "VK_GROUP_ID", "42");

        String result = new BotConfig(env::get).toString();

        assertEquals("BotConfig{groupId=42, apiVersion='5.199'}", result);
    }
}
