package com.tsu.tsueventhubapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;


@Data
@Builder
public class StudentResponse {
    private UUID id;
    private String name;
    private String email;
}
