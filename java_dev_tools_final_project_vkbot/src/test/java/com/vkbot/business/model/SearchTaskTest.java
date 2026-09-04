package com.vkbot.business.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchTaskTest {

    @Test
    void oneArgConstructor_setsDefaultsAndActiveTrue() {
        SearchTask task = new SearchTask(1L);

        assertEquals(1L, task.getUserId());
        assertEquals(0, task.getMinExperience());
        assertEquals(0L, task.getMinSalary());
        assertTrue(task.isActive());
    }

    @Test
    void noArgsConstructor_andSetters_workTogether() {
        SearchTask task = new SearchTask();
        task.setId(1L);
        task.setUserId(2L);
        task.setKeyword("java");
        task.setRegionCode("77");
        task.setMinExperience(3);
        task.setMinSalary(90000L);
        task.setActive(false);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        assertEquals(1L, task.getId());
        assertEquals("java", task.getKeyword());
        assertEquals("77", task.getRegionCode());
        assertEquals(3, task.getMinExperience());
        assertEquals(90000L, task.getMinSalary());
        assertTrue(!task.isActive());
        assertEquals(now, task.getCreatedAt());
        assertEquals(now, task.getUpdatedAt());
    }

    @Test
    void builder_setsAllFields() {
        SearchTask task = SearchTask.builder()
                .id(1L)
                .userId(2L)
                .keyword("java")
                .regionCode("77")
                .minExperience(2)
                .minSalary(70000L)
                .active(true)
                .build();

        assertEquals("java", task.getKeyword());
        assertEquals(2, task.getMinExperience());
        assertTrue(task.isActive());
    }

    @Test
    void allArgsConstructor_setsEveryField() {
        LocalDateTime now = LocalDateTime.now();
        SearchTask task = new SearchTask(1L, 2L, "java", "77", 2, 70000L, true, now, now);

        assertEquals(1L, task.getId());
        assertEquals("java", task.getKeyword());
        assertEquals(70000L, task.getMinSalary());
    }

    @Test
    void equalsAndHashCode_areConsistentForSameData() {
        SearchTask a = new SearchTask(1L);
        a.setId(10L);
        SearchTask b = new SearchTask(1L);
        b.setId(10L);
        SearchTask c = new SearchTask(1L);
        c.setId(11L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toString_includesFieldValues() {
        SearchTask task = new SearchTask(1L);
        task.setId(10L);

        assertNotNull(task.toString());
        assertTrue(task.toString().contains("id=10"));
    }

    @Test
    void equals_handlesSelfNullAndDifferentType() {
        SearchTask task = new SearchTask(1L);

        assertEquals(task, task);
        assertNotEquals(task, null);
        assertNotEquals(task, "not a task");
    }
}
