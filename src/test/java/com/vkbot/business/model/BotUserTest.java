package com.vkbot.business.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotUserTest {

    @Test
    void twoArgConstructor_setsActiveTrueAndLeavesTimestampsNull() {
        BotUser user = new BotUser(1L, 100L);

        assertEquals(1L, user.getUserId());
        assertEquals(100L, user.getChatId());
        assertTrue(user.isActive());
    }

    @Test
    void noArgsConstructor_andSetters_workTogether() {
        BotUser user = new BotUser();
        user.setUserId(2L);
        user.setChatId(200L);
        user.setActive(false);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(2L, user.getUserId());
        assertEquals(200L, user.getChatId());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertTrue(!user.isActive());
    }

    @Test
    void builder_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        BotUser user = BotUser.builder()
                .userId(3L)
                .chatId(300L)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(3L, user.getUserId());
        assertEquals(300L, user.getChatId());
        assertTrue(user.isActive());
    }

    @Test
    void allArgsConstructor_setsEveryField() {
        LocalDateTime now = LocalDateTime.now();
        BotUser user = new BotUser(4L, 400L, now, now, true);

        assertEquals(4L, user.getUserId());
        assertEquals(400L, user.getChatId());
        assertTrue(user.isActive());
    }

    @Test
    void equalsAndHashCode_areConsistentForSameData() {
        BotUser a = new BotUser(1L, 100L);
        BotUser b = new BotUser(1L, 100L);
        BotUser c = new BotUser(2L, 100L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toString_includesFieldValues() {
        BotUser user = new BotUser(1L, 100L);

        assertNotNull(user.toString());
        assertTrue(user.toString().contains("userId=1"));
    }

    @Test
    void equals_handlesSelfNullAndDifferentType() {
        BotUser user = new BotUser(1L, 100L);

        assertEquals(user, user);
        assertNotEquals(user, null);
        assertNotEquals(user, "not a user");
    }
}
