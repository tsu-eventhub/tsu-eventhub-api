package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.CompanyResponse;
import com.tsu.tsueventhubapi.enumeration.Role;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.CompanyRepository;
import com.tsu.tsueventhubapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public List<CompanyResponse> getCompaniesForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return getCompaniesForCurrentUser(user);
    }

    public List<CompanyResponse> getCompaniesForCurrentUser(User user) {
        if (user.getRole() == Role.DEAN) {
            return getAllCompanies();
        }

        if (user.getRole() == Role.MANAGER) {
            if (user.getCompany() != null) return List.of();
            return getAllCompanies();
        }

        return List.of();
    }

    public List<CompanyResponse> getCompaniesForRegistration() {
        return getAllCompanies();
    }

    private List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(c -> CompanyResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();
    }
}
