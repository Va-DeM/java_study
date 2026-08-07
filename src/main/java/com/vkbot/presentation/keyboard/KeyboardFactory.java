package com.vkbot.presentation.keyboard;

import com.vk.api.sdk.objects.messages.Keyboard;
import com.vk.api.sdk.objects.messages.KeyboardButton;
import com.vk.api.sdk.objects.messages.KeyboardButtonActionText;
import com.vk.api.sdk.objects.messages.KeyboardButtonActionTextType;
import com.vk.api.sdk.objects.messages.KeyboardButtonColor;
import com.vkbot.business.model.SearchTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyboardFactory {

    private KeyboardFactory() {
    }

    public static Keyboard mainMenu() {
        return new Keyboard()
                .setOneTime(false)
                .setInline(false)
                .setButtons(List.of(
                        row(button("Создать заявку на поиск вакансий", KeyboardButtonColor.PRIMARY)),
                        row(button("Текущие заявки на поиск", KeyboardButtonColor.DEFAULT)),
                        row(button("Обновить задачу на поиск", KeyboardButtonColor.DEFAULT)),
                        row(button("Удалить заявку на поиск", KeyboardButtonColor.NEGATIVE))
                ));
    }

    public static Keyboard taskEditMenu(SearchTask task) {
        String regionLabel = fieldLabel("Регион", task.getRegionCode());
        String experienceLabel = task.getMinExperience() != null && task.getMinExperience() > 0
                ? fieldLabel("Минимальный опыт", task.getMinExperience())
                : "Минимальный опыт";
        String salaryLabel = task.getMinSalary() != null && task.getMinSalary() > 0
                ? fieldLabel("Минимальная зарплата", task.getMinSalary())
                : "Минимальная зарплата";
        String keywordLabel = fieldLabel("Слово для поиска", task.getKeyword());

        return new Keyboard()
                .setOneTime(false)
                .setInline(false)
                .setButtons(List.of(
                        row(button(regionLabel, KeyboardButtonColor.DEFAULT)),
                        row(button(experienceLabel, KeyboardButtonColor.DEFAULT)),
                        row(button(salaryLabel, KeyboardButtonColor.DEFAULT)),
                        row(button(keywordLabel, KeyboardButtonColor.DEFAULT)),
                        row(button("Готово", KeyboardButtonColor.POSITIVE))
                ));
    }

    public static Keyboard regionPage(List<Map.Entry<String, String>> pageEntries, boolean hasNext) {
        List<List<KeyboardButton>> rows = new ArrayList<>();
        for (Map.Entry<String, String> entry : pageEntries) {
            String displayCode = entry.getKey().length() < 2 ? "0" + entry.getKey() : entry.getKey();
            rows.add(row(button(entry.getValue() + ", " + displayCode, KeyboardButtonColor.DEFAULT)));
        }
        if (hasNext) {
            rows.add(row(button("Далее", KeyboardButtonColor.PRIMARY)));
        }
        return new Keyboard()
                .setOneTime(false)
                .setInline(false)
                .setButtons(rows);
    }

    public static Keyboard experienceOptions() {
        return new Keyboard()
                .setOneTime(true)
                .setInline(false)
                .setButtons(List.of(
                        row(button("1 год", KeyboardButtonColor.DEFAULT),
                                button("2 года", KeyboardButtonColor.DEFAULT),
                                button("3 года", KeyboardButtonColor.DEFAULT))
                ));
    }

    public static Keyboard salaryOptions() {
        return new Keyboard()
                .setOneTime(true)
                .setInline(false)
                .setButtons(List.of(
                        row(button("50000", KeyboardButtonColor.DEFAULT),
                                button("70000", KeyboardButtonColor.DEFAULT),
                                button("100000", KeyboardButtonColor.DEFAULT))
                ));
    }

    public static Keyboard keywordOptions() {
        return new Keyboard()
                .setOneTime(true)
                .setInline(false)
                .setButtons(List.of(
                        row(button("Java", KeyboardButtonColor.DEFAULT),
                                button("Python", KeyboardButtonColor.DEFAULT),
                                button("JavaScript", KeyboardButtonColor.DEFAULT)),
                        row(button("Frontend", KeyboardButtonColor.DEFAULT),
                                button("Backend", KeyboardButtonColor.DEFAULT))
                ));
    }

    public static Keyboard numberedSelection(int count) {
        List<List<KeyboardButton>> rows = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            rows.add(row(button(String.valueOf(i), KeyboardButtonColor.DEFAULT)));
        }
        return new Keyboard()
                .setOneTime(true)
                .setInline(false)
                .setButtons(rows);
    }

    private static String fieldLabel(String base, Object value) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            return base;
        }
        return base + "[" + value + "]";
    }

    private static KeyboardButton button(String label, KeyboardButtonColor color) {
        return new KeyboardButton()
                .setAction(new KeyboardButtonActionText()
                        .setLabel(label)
                        .setType(KeyboardButtonActionTextType.TEXT))
                .setColor(color);
    }

    private static List<KeyboardButton> row(KeyboardButton... buttons) {
        return List.of(buttons);
    }
}
