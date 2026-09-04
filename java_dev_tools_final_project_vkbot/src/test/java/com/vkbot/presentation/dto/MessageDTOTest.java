package com.vkbot.presentation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageDTOTest {

    @Test
    void threeArgConstructor_setsUserIdChatIdAndText() {
        MessageDTO message = new MessageDTO(1L, 100L, "/start");

        assertEquals(1L, message.getUserId());
        assertEquals(100L, message.getChatId());
        assertEquals("/start", message.getText());
        assertNull(message.getMessageId());
    }

    @Test
    void noArgsConstructor_andSetters_workTogether() {
        MessageDTO message = new MessageDTO();
        message.setUserId(1L);
        message.setChatId(100L);
        message.setText("/start");
        message.setMessageId(5L);
        message.setTimestamp(1000L);

        assertEquals(5L, message.getMessageId());
        assertEquals(1000L, message.getTimestamp());
    }

    @Test
    void builder_setsAllFields() {
        MessageDTO message = MessageDTO.builder()
                .userId(1L)
                .chatId(100L)
                .text("/start")
                .messageId(5L)
                .timestamp(1000L)
                .build();

        assertEquals(1L, message.getUserId());
        assertEquals(5L, message.getMessageId());
    }

    @Test
    void allArgsConstructor_setsEveryField() {
        MessageDTO message = new MessageDTO(1L, 100L, "/start", 5L, 1000L);

        assertEquals(1L, message.getUserId());
        assertEquals(100L, message.getChatId());
        assertEquals("/start", message.getText());
        assertEquals(5L, message.getMessageId());
        assertEquals(1000L, message.getTimestamp());
    }

    @Test
    void equalsAndHashCode_areConsistentForSameData() {
        MessageDTO a = new MessageDTO(1L, 100L, "/start", 5L, 1000L);
        MessageDTO b = new MessageDTO(1L, 100L, "/start", 5L, 1000L);
        MessageDTO c = new MessageDTO(2L, 100L, "/start", 5L, 1000L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toString_includesFieldValues() {
        MessageDTO message = new MessageDTO(1L, 100L, "/start");

        assertNotNull(message.toString());
        assertTrue(message.toString().contains("userId=1"));
    }

    @Test
    void equals_handlesSelfNullAndDifferentType() {
        MessageDTO message = new MessageDTO(1L, 100L, "/start");

        assertEquals(message, message);
        assertNotEquals(message, null);
        assertNotEquals(message, "not a message");
    }
}
