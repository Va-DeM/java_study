package com.vkbot.business.service;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Keyboard;

public interface VKApiService {
    void sendMessage(Long userId, String message) throws ApiException, ClientException;

    void sendMessage(Long userId, String message, Keyboard keyboard) throws ApiException, ClientException;
}

