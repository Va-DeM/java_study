package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.business.model.SearchTask;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import com.vkbot.util.RegionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class RegionCommand implements BotCommand {
    private static final String MENU_LABEL = "регион";
    // Метка кнопки региона выглядит как "Название, 05" (см. KeyboardFactory.regionPage)
    private static final Pattern LABEL_PATTERN = Pattern.compile("^.+,\\s*(\\d{1,2})$");

    private final VKApiService vkApiService;

    @Override
    public boolean supports(String input) {
        return input.startsWith("/region")
                || input.toLowerCase().startsWith(MENU_LABEL)
                || parseRegionLabel(input) != null;
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing RegionCommand for user: {}", message.getUserId());
        String text = message.getText();

        if (text.toLowerCase().startsWith(MENU_LABEL) && !text.startsWith("/region")) {
            NextCommand.showFirstRegionPage(vkApiService, message.getUserId());
            return;
        }

        String regionCode;
        if (text.startsWith("/region")) {
            String[] parts = text.split(" ", 2);
            if (parts.length < 2) {
                send(message.getUserId(), "❌ Пожалуйста, укажите код региона. Пример: /region 10");
                return;
            }
            regionCode = normalizeCode(parts[1].trim());
        } else {
            regionCode = parseRegionLabel(text);
        }

        if (regionCode == null || !RegionUtil.regionExists(regionCode)) {
            send(message.getUserId(), "❌ Регион не найден.");
            return;
        }

        SearchTask task = NewTaskCommand.getUserTask(message.getUserId());
        if (task == null) {
            task = UpdateTaskCommand.getEditingTask(message.getUserId());
        }

        if (task == null) {
            send(message.getUserId(), "❌ Сначала создайте новую заявку или выберите задачу для обновления.");
            return;
        }

        task.setRegionCode(regionCode);
        try {
            vkApiService.sendMessage(message.getUserId(),
                    "✅ Регион установлен: " + RegionUtil.getRegionName(regionCode),
                    KeyboardFactory.taskEditMenu(task));
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    private String parseRegionLabel(String text) {
        Matcher matcher = LABEL_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return null;
        }
        String code = normalizeCode(matcher.group(1));
        return RegionUtil.regionExists(code) ? code : null;
    }

    private String normalizeCode(String rawCode) {
        try {
            return String.valueOf(Integer.parseInt(rawCode));
        } catch (NumberFormatException e) {
            return rawCode;
        }
    }

    private void send(Long userId, String text) {
        try {
            vkApiService.sendMessage(userId, text);
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.REGION;
    }
}
