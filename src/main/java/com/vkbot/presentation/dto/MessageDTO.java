package com.vkbot.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Long userId;
    private Long chatId;
    private String text;
    private Long messageId;
    private Long timestamp;

    public MessageDTO(Long userId, Long chatId, String text) {
        this.userId = userId;
        this.chatId = chatId;
        this.text = text;
    }
}

