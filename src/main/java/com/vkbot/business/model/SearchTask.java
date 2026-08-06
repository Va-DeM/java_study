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
public class SearchTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String keyword;
    private String regionCode;
    private Integer minExperience;
    private Long minSalary;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public SearchTask(Long userId) {
        this.userId = userId;
        this.minExperience = 0;
        this.minSalary = 0L;
        this.active = true;
    }
}

