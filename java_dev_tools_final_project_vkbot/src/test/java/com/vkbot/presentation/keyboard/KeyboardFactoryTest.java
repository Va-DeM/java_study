package com.vkbot.presentation.keyboard;

import com.vk.api.sdk.objects.messages.Keyboard;
import com.vkbot.business.model.SearchTask;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyboardFactoryTest {

    @Test
    void mainMenu_hasFourButtonsOnePerRow() {
        Keyboard keyboard = KeyboardFactory.mainMenu();

        assertEquals(4, keyboard.getButtons().size());
        keyboard.getButtons().forEach(row -> assertEquals(1, row.size()));
        assertTrue(keyboard.toString().contains("Создать заявку на поиск вакансий"));
        assertTrue(keyboard.toString().contains("Удалить заявку на поиск"));
        assertFalse(keyboard.getOneTime());
    }

    @Test
    void taskEditMenu_showsPlainLabelsWhenFieldsUnset() {
        SearchTask task = new SearchTask(1L);

        Keyboard keyboard = KeyboardFactory.taskEditMenu(task);

        String json = keyboard.toString();
        assertTrue(json.contains("\"Регион\""));
        assertTrue(json.contains("\"Минимальный опыт\""));
        assertTrue(json.contains("\"Минимальная зарплата\""));
        assertTrue(json.contains("\"Слово для поиска\""));
        assertTrue(json.contains("\"Готово\""));
    }

    @Test
    void taskEditMenu_showsBracketedValuesWhenFieldsSet() {
        SearchTask task = new SearchTask(1L);
        task.setRegionCode("10");
        task.setMinExperience(2);
        task.setMinSalary(70000L);
        task.setKeyword("java");

        String json = KeyboardFactory.taskEditMenu(task).toString();

        assertTrue(json.contains("Регион[10]"));
        assertTrue(json.contains("Минимальный опыт[2]"));
        assertTrue(json.contains("Минимальная зарплата[70000]"));
        assertTrue(json.contains("Слово для поиска[java]"));
    }

    @Test
    void taskEditMenu_treatsZeroExperienceAndSalaryAsUnset() {
        SearchTask task = new SearchTask(1L);
        task.setMinExperience(0);
        task.setMinSalary(0L);

        String json = KeyboardFactory.taskEditMenu(task).toString();

        assertFalse(json.contains("Минимальный опыт["));
        assertFalse(json.contains("Минимальная зарплата["));
    }

    @Test
    void regionPage_rendersOneButtonPerRegionWithZeroPaddedSingleDigitCodes() {
        List<Map.Entry<String, String>> entries = List.of(
                Map.entry("1", "Республика Адыгея"),
                Map.entry("10", "Республика Карелия")
        );

        String json = KeyboardFactory.regionPage(entries, false).toString();

        assertTrue(json.contains("Республика Адыгея, 01"));
        assertTrue(json.contains("Республика Карелия, 10"));
        assertFalse(json.contains("Далее"));
    }

    @Test
    void regionPage_includesNextButtonOnlyWhenRequested() {
        List<Map.Entry<String, String>> entries = List.of(Map.entry("1", "Республика Адыгея"));

        assertTrue(KeyboardFactory.regionPage(entries, true).toString().contains("Далее"));
        assertFalse(KeyboardFactory.regionPage(entries, false).toString().contains("Далее"));
    }

    @Test
    void experienceOptions_hasThreeOneTimeButtons() {
        Keyboard keyboard = KeyboardFactory.experienceOptions();

        assertTrue(keyboard.getOneTime());
        String json = keyboard.toString();
        assertTrue(json.contains("1 год"));
        assertTrue(json.contains("2 года"));
        assertTrue(json.contains("3 года"));
    }

    @Test
    void salaryOptions_matchesValuesFromTechSpec() {
        String json = KeyboardFactory.salaryOptions().toString();

        assertTrue(json.contains("\"50000\""));
        assertTrue(json.contains("\"70000\""));
        assertTrue(json.contains("\"100000\""));
    }

    @Test
    void keywordOptions_includesPopularKeywords() {
        String json = KeyboardFactory.keywordOptions().toString();

        assertTrue(json.contains("Java"));
        assertTrue(json.contains("Python"));
        assertTrue(json.contains("Backend"));
    }

    @Test
    void numberedSelection_generatesOneButtonPerNumberStartingAtOne() {
        Keyboard keyboard = KeyboardFactory.numberedSelection(3);

        assertEquals(3, keyboard.getButtons().size());
        String json = keyboard.toString();
        assertTrue(json.contains("\"1\""));
        assertTrue(json.contains("\"2\""));
        assertTrue(json.contains("\"3\""));
    }
}
