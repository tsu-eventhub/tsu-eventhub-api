package com.tsu.tsueventhubapi.dto;

import com.tsu.tsueventhubapi.enumeration.Role;
import com.tsu.tsueventhubapi.enumeration.Status;
import com.tsu.tsueventhubapi.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private Status status;
    private String telegramUsername;
    private CompanyResponse company;

    public static UserResponse fromEntity(User user) {
        CompanyResponse companyResponse = null;
        if (user.getRole() == Role.MANAGER && user.getCompany() != null) {
            companyResponse = CompanyResponse.builder()
                    .id(user.getCompany().getId())
                    .name(user.getCompany().getName())
                    .build();
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .telegramUsername(user.getTelegramUsername())
                .company(companyResponse)
                .build();
    }
}
