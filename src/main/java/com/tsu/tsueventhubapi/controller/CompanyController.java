package com.tsu.tsueventhubapi.controller;


import com.tsu.tsueventhubapi.dto.CompanyResponse;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import com.tsu.tsueventhubapi.service.CompanyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/companies")
@Tag(name = "Companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getCompanies(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<CompanyResponse> companies;

        if (currentUser != null) {
            companies = companyService.getCompaniesForUser(currentUser.getId());
        } else {
            companies = companyService.getCompaniesForRegistration();
        }

        return ResponseEntity.ok(companies);
    }
}
