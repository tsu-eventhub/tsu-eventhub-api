package com.tsu.tsueventhubapi.dto;

import com.tsu.tsueventhubapi.enumeration.Role;
import com.tsu.tsueventhubapi.enumeration.Status;
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
    private String telegramId;
    private CompanyResponse company;
}
