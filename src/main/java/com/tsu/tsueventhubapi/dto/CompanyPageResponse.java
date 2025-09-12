package com.tsu.tsueventhubapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompanyPageResponse {
    private int totalPages;
    private long totalElements;
    private int size;
    private List<CompanyResponse> content;
}
