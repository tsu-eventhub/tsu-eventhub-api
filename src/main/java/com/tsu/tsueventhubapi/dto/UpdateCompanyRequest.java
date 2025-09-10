package com.tsu.tsueventhubapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCompanyRequest {

    @NotBlank(message = "Company name cannot be empty")
    private String name;
}
