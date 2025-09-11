package com.tsu.tsueventhubapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationResponse {
    private UUID eventId;
    private UUID studentId;
    private Instant registeredAt;
}
