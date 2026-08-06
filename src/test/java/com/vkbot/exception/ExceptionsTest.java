package com.vkbot.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionsTest {

    @Test
    void apiException_messageOnly() {
        ApiException e = new ApiException("boom");

        assertEquals("boom", e.getMessage());
        assertTrue(e instanceof BotException);
    }

    @Test
    void apiException_messageAndCause() {
        Throwable cause = new RuntimeException("root cause");

        ApiException e = new ApiException("boom", cause);

        assertEquals("boom", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    void botException_messageOnly() {
        BotException e = new BotException("boom");

        assertEquals("boom", e.getMessage());
        assertTrue(e instanceof RuntimeException);
    }

    @Test
    void botException_messageAndCause() {
        Throwable cause = new RuntimeException("root cause");

        BotException e = new BotException("boom", cause);

        assertSame(cause, e.getCause());
    }

    @Test
    void taskNotFoundException_carriesMessage() {
        TaskNotFoundException e = new TaskNotFoundException("task 5 not found");

        assertEquals("task 5 not found", e.getMessage());
        assertTrue(e instanceof BotException);
    }

    @Test
    void userNotFoundException_carriesMessage() {
        UserNotFoundException e = new UserNotFoundException("user 5 not found");

        assertEquals("user 5 not found", e.getMessage());
        assertTrue(e instanceof BotException);
    }
}
