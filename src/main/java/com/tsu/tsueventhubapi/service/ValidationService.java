package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.RegisterRequest;
import com.tsu.tsueventhubapi.enumeration.Role;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {
    private static final String TELEGRAM_REGEX = "^@[a-zA-Z0-9_]{1,64}$";

    public void validateRegisterRequest(RegisterRequest request) {
        validateTelegramByRole(request.getRole(), request.getTelegramUsername());
    }

    public void validateProfileUpdate(Role role, String telegramUsername) {
        validateTelegramByRole(role, telegramUsername);
    }

    private void validateTelegramByRole(Role role, String telegramUsername) {
        if (role == Role.MANAGER) {
            if (telegramUsername == null || telegramUsername.isBlank() || !telegramUsername.matches(TELEGRAM_REGEX)) {
                throw new IllegalArgumentException("Telegram username is required and must start with @ and contain 1-64 letters, numbers or underscores");
            }
        } else if (role == Role.STUDENT) {
            if (telegramUsername != null && !telegramUsername.isBlank() && !telegramUsername.matches(TELEGRAM_REGEX)) {
                throw new IllegalArgumentException("Telegram username must start with @ and contain 1-64 letters, numbers or underscores");
            }
        } else if (role == Role.DEAN) {
            if (telegramUsername != null && !telegramUsername.isBlank()) {
                throw new IllegalArgumentException("The dean's office can't provide Telegram username");
            }
        }
    }
}
