package com.tsu.tsueventhubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateEventRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Instant startTime;

    private Instant endTime;

    @NotBlank
    private String location;

    private Instant registrationDeadline;
}
