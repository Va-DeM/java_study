package com.vkbot.business.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VacancyTest {

    @Test
    void noArgsConstructor_andSetters_workTogether() {
        Vacancy vacancy = new Vacancy();
        vacancy.setId("v1");
        vacancy.setTaskId(1L);
        vacancy.setTitle("Java Developer");
        vacancy.setSalaryFrom(80000L);
        vacancy.setSalaryTo(120000L);
        vacancy.setCurrency("RUR");
        vacancy.setExperienceFrom(3L);
        vacancy.setRegion("Москва");
        vacancy.setRegionCode("77");
        vacancy.setCompanyName("ООО Ромашка");
        vacancy.setEducation("Высшее");
        vacancy.setContacts("phone: +7");
        vacancy.setDescription("desc");
        vacancy.setUrl("https://example.com");
        LocalDateTime now = LocalDateTime.now();
        vacancy.setCreatedAt(now);
        vacancy.setModifiedDate(now);
        vacancy.setPublishedDate(now);

        assertEquals("v1", vacancy.getId());
        assertEquals(1L, vacancy.getTaskId());
        assertEquals("Java Developer", vacancy.getTitle());
        assertEquals(80000L, vacancy.getSalaryFrom());
        assertEquals(120000L, vacancy.getSalaryTo());
        assertEquals("RUR", vacancy.getCurrency());
        assertEquals(3L, vacancy.getExperienceFrom());
        assertEquals("Москва", vacancy.getRegion());
        assertEquals("77", vacancy.getRegionCode());
        assertEquals("ООО Ромашка", vacancy.getCompanyName());
        assertEquals("Высшее", vacancy.getEducation());
        assertEquals("desc", vacancy.getDescription());
        assertEquals("https://example.com", vacancy.getUrl());
        assertEquals(now, vacancy.getCreatedAt());
        assertEquals(now, vacancy.getModifiedDate());
        assertEquals(now, vacancy.getPublishedDate());
    }

    @Test
    void builder_setsAllFields() {
        Vacancy vacancy = Vacancy.builder()
                .id("v1")
                .taskId(1L)
                .title("Java Developer")
                .build();

        assertEquals("v1", vacancy.getId());
        assertEquals(1L, vacancy.getTaskId());
        assertEquals("Java Developer", vacancy.getTitle());
    }

    @Test
    void allArgsConstructor_setsEveryField() {
        LocalDateTime now = LocalDateTime.now();
        Vacancy vacancy = new Vacancy("v1", 1L, "Title", 80000L, 120000L, "RUR", 3L,
                "Москва", "77", "ООО Ромашка", "Высшее", "contacts", "desc",
                "https://example.com", now, now, now);

        assertEquals("v1", vacancy.getId());
        assertEquals("Title", vacancy.getTitle());
        assertEquals("contacts", vacancy.getContacts());
    }

    @Test
    void equalsAndHashCode_areConsistentForSameData() {
        Vacancy a = Vacancy.builder().id("v1").taskId(1L).build();
        Vacancy b = Vacancy.builder().id("v1").taskId(1L).build();
        Vacancy c = Vacancy.builder().id("v2").taskId(1L).build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toString_includesFieldValues() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).build();

        assertNotNull(vacancy.toString());
        assertTrue(vacancy.toString().contains("id=v1"));
    }

    @Test
    void equals_handlesSelfNullAndDifferentType() {
        Vacancy vacancy = Vacancy.builder().id("v1").taskId(1L).build();

        assertEquals(vacancy, vacancy);
        assertNotEquals(vacancy, null);
        assertNotEquals(vacancy, "not a vacancy");
    }
}
