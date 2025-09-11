package com.tsu.tsueventhubapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseSummary {
    private UUID id;
    private String title;
    private Instant startTime;
    private String location;
    private CompanyResponse company;
}
