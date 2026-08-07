package com.vkbot.presentation.command.impl;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vkbot.presentation.command.BotCommand;
import com.vkbot.presentation.command.CommandType;
import com.vkbot.presentation.dto.MessageDTO;
import com.vkbot.business.service.VKApiService;
import com.vkbot.presentation.keyboard.KeyboardFactory;
import com.vkbot.util.RegionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class NextCommand implements BotCommand {
    private static final int REGIONS_PER_PAGE = 5;
    private static final Map<Long, Integer> userRegionPages = new HashMap<>();

    private final VKApiService vkApiService;

    @Override
    public boolean supports(String input) {
        return input.equalsIgnoreCase("далее") || input.equalsIgnoreCase("/next");
    }

    @Override
    public void execute(MessageDTO message) {
        log.info("Executing NextCommand for user: {}", message.getUserId());

        int currentPage = userRegionPages.getOrDefault(message.getUserId(), 0);
        List<String> regionCodes = sortedRegionCodes();
        int totalPages = (int) Math.ceil((double) regionCodes.size() / REGIONS_PER_PAGE);
        int nextPage = currentPage + 1;

        if (nextPage >= totalPages) {
            try {
                vkApiService.sendMessage(message.getUserId(), "📍 Вы уже на последней странице регионов.");
            } catch (ApiException | ClientException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        userRegionPages.put(message.getUserId(), nextPage);
        showRegionPage(vkApiService, message.getUserId(), nextPage, regionCodes);
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.NEXT;
    }

    public static void showFirstRegionPage(VKApiService vkApiService, Long userId) {
        userRegionPages.put(userId, 0);
        showRegionPage(vkApiService, userId, 0, sortedRegionCodes());
    }

    private static void showRegionPage(VKApiService vkApiService, Long userId, int pageNumber, List<String> regionCodes) {
        int startIdx = pageNumber * REGIONS_PER_PAGE;
        int endIdx = Math.min(startIdx + REGIONS_PER_PAGE, regionCodes.size());
        boolean hasNext = endIdx < regionCodes.size();

        StringBuilder response = new StringBuilder();
        response.append("📍 Регионы (страница ").append(pageNumber + 1).append(")\n\n");

        List<Map.Entry<String, String>> pageEntries = new ArrayList<>();
        for (int i = startIdx; i < endIdx; i++) {
            String code = regionCodes.get(i);
            String name = RegionUtil.getRegionName(code);
            response.append("[").append(code).append("] ").append(name).append("\n");
            pageEntries.add(Map.entry(code, name));
        }

        response.append("\nВыберите регион кнопкой ниже или командой /region [код].");

        try {
            vkApiService.sendMessage(userId, response.toString(), KeyboardFactory.regionPage(pageEntries, hasNext));
        } catch (ApiException | ClientException e) {
            log.error("Error sending message", e);
        }
    }

    private static List<String> sortedRegionCodes() {
        return RegionUtil.getAllRegions().keySet().stream()
                .sorted(Comparator.comparingInt(Integer::parseInt))
                .toList();
    }
}
