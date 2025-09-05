package com.tsu.tsueventhubapi.dto;

import com.tsu.tsueventhubapi.enumeration.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Telegram username is required")
    @Pattern(
            regexp = "^@[a-zA-Z0-9_]{1,64}$",
            message = "Telegram username must start with @ and contain 1-64 letters, numbers or underscores"
    )
    private String telegramId;

    private Long companyId;
}
