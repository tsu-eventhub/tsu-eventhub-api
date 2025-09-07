package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.RegisterRequest;
import com.tsu.tsueventhubapi.enumeration.Role;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {
    private static final String TELEGRAM_REGEX = "^@[a-zA-Z0-9_]{1,64}$";

    public void validateRegisterRequest(RegisterRequest request) {
        String telegram = request.getTelegramId();

        if (request.getRole() == Role.MANAGER) {
            if (telegram == null || telegram.isBlank() || !telegram.matches(TELEGRAM_REGEX)) {
                throw new IllegalArgumentException("Telegram username is required and must start with @ and contain 1-64 letters, numbers or underscores");
            }
        } else if (request.getRole() == Role.STUDENT) {
            if (telegram != null && !telegram.isBlank() && !telegram.matches(TELEGRAM_REGEX)) {
                throw new IllegalArgumentException("Telegram username must start with @ and contain 1-64 letters, numbers or underscores");
            }
        } else if (request.getRole() == Role.DEAN) {
            if (telegram != null && !telegram.isBlank()) {
                throw new IllegalArgumentException("The dean's office can't provide Telegram username");
            }
        }
    }
}
