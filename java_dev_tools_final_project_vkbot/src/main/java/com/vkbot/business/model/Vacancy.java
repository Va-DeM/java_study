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
public class Vacancy implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private Long taskId;
    private String title;

    // В БД BIGINT, поэтому Long — корректно
    private Long salaryFrom;
    private Long salaryTo;

    private String currency;
    // Лучше Long, чтобы избежать переполнения при парсинге из API
    private Long experienceFrom;

    private String region;
    private String regionCode;
    private String companyName;
    private String education;
    private String contacts;
    private String description;
    private String url;

    private LocalDateTime createdAt;

    // ВАЖНО: это поле НЕ должно заполняться из API.
    // Если нужно хранить дату модификации из API — сделай отдельное поле, например `apiModifiedDate`.
    private LocalDateTime modifiedDate;

    // Дата публикации вакансии (из API)
    private LocalDateTime publishedDate;
}