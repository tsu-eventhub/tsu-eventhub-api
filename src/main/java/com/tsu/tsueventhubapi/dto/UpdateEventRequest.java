package com.tsu.tsueventhubapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventRequest {

    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private String location;
    private Instant registrationDeadline;
}
