package com.tsu.tsueventhubapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentPageResponse {
    private int totalPages;
    private long totalElements;
    private int size;
    private List<StudentResponse> content;
}
