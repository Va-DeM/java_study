package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Keyboard;
import com.vkbot.business.model.SearchTask;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class SalaryCommand implements BotCommand {
    private static final String MENU_LABEL = "минимальная зарплата";
    // Точное множество значений с клавиатуры (не общий "любое число") —
    // чтобы не конфликтовать с выбором заявки по номеру (голое маленькое число).
    private static final Set<String> QUICK_OPTIONS = Set.of("50000", "70000", "100000");

    private final VKApiService vkApiService;

    @Override
    public boolean supports(String input) {
        return input.startsWith("/minSalary")
                || input.toLowerCase().startsWith(MENU_LABEL)
                || QUICK_OPTIONS.contains(input);
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing SalaryCommand for user: {}", message.getUserId());
        String text = message.getText();

        if (text.toLowerCase().startsWith(MENU_LABEL)) {
            send(message.getUserId(), "Выберите минимальную зарплату:", KeyboardFactory.salaryOptions());
            return;
        }

        Long salary;
        if (QUICK_OPTIONS.contains(text)) {
            salary = Long.parseLong(text);
        } else {
            String[] parts = text.split(" ", 2);
            if (parts.length < 2) {
                send(message.getUserId(), "❌ Пожалуйста, укажите минимальную зарплату. Пример: /minSalary 50000", null);
                return;
            }
            try {
                salary = Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                send(message.getUserId(), "❌ Пожалуйста, введите корректное значение (число).", null);
                return;
            }
        }

        if (salary < 0) {
            send(message.getUserId(), "❌ Зарплата не может быть отрицательной.", null);
            return;
        }

        SearchTask task = NewTaskCommand.getUserTask(message.getUserId());
        if (task == null) {
            task = UpdateTaskCommand.getEditingTask(message.getUserId());
        }

        if (task != null) {
            task.setMinSalary(salary);
            send(message.getUserId(), "✅ Минимальная зарплата установлена: " + salary + " руб.", KeyboardFactory.taskEditMenu(task));
        } else {
            send(message.getUserId(), "❌ Сначала создайте новую заявку или выберите задачу для обновления.", KeyboardFactory.mainMenu());
        }
    }

    private void send(Long userId, String text, Keyboard keyboard) {
        try {
            if (keyboard != null) {
                vkApiService.sendMessage(userId, text, keyboard);
            } else {
                vkApiService.sendMessage(userId, text);
            }
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.MIN_SALARY;
    }
}
