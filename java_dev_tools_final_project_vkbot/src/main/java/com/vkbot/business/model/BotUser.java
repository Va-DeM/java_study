package com.vkbot.business.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long chatId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean active;

    public BotUser(Long userId, Long chatId) {
        this.userId = userId;
        this.chatId = chatId;
        this.active = true;
    }
}

