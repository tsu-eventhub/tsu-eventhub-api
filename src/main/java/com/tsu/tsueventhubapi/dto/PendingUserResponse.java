package com.tsu.tsueventhubapi.dto;

import com.tsu.tsueventhubapi.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PendingUserResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private String telegramUsername;
    private String company;
}
