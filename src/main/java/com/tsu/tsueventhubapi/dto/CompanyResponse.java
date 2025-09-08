package com.tsu.tsueventhubapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CompanyResponse {
    private UUID id;
    private String name;
}
