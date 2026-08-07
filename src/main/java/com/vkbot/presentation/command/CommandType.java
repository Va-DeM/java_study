package com.vkbot.presentation.command;

public enum CommandType {
    DEFAULT("Начать", "/start"),
    MAIN_MENU("Показать меню", "/menu"),
    NEW_TASK("Создать заявку на поиск вакансий", "/newTask"),
    CURRENT_TASKS("Текущие заявки на поиск", "/current"),
    DELETE_TASK("Удалить заявку на поиск", "/delete"),
    UPDATE_TASK("Обновить задачу на поиск", "/update"),
    REGION("Регион", "/region"),
    MIN_EXP("Минимальный опыт", "/minExp"),
    MIN_SALARY("Минимальная зарплата", "/minSalary"),
    KEYWORD("Слово для поиска", "/keyword"),
    DONE("Готово", "/done"),
    NEXT("Далее", "/next"),
    NONE(null, null);

    private final String buttonText;
    private final String command;

    CommandType(String buttonText, String command) {
        this.buttonText = buttonText;
        this.command = command;
    }

    public String getButtonText() {
        return buttonText;
    }

    public String getCommand() {
        return command;
    }
}

