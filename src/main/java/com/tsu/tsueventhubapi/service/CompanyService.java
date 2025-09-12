package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.CompanyResponse;
import com.tsu.tsueventhubapi.dto.CreateCompanyRequest;
import com.tsu.tsueventhubapi.dto.UpdateCompanyRequest;
import com.tsu.tsueventhubapi.enumeration.Role;
import com.tsu.tsueventhubapi.exception.ForbiddenException;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.Company;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.CompanyRepository;
import com.tsu.tsueventhubapi.repository.UserRepository;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public Page<CompanyResponse> getCompaniesForUser(UUID userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!"APPROVED".equals(user.getStatus().name())) {
            throw new ForbiddenException("Only approved users can access this resource");
        }
        
        return getCompaniesForCurrentUser(user, page, size);
    }

    public Page<CompanyResponse> getCompaniesForCurrentUser(User user, int page, int size) {
        if (user.getRole() == Role.DEAN) {
            return getAllCompanies(page, size);
        }

        if (user.getRole() == Role.MANAGER || user.getRole() == Role.STUDENT) {
            throw new ForbiddenException("Access Denied");
        }
        
        return Page.empty();
    }

    public Page<CompanyResponse> getCompaniesForRegistration(int page, int size) {
        return getAllCompanies(page, size);
    }

    private Page<CompanyResponse> getAllCompanies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Company> companiesPage = companyRepository.findAll(pageable);

        return companiesPage.map(c -> CompanyResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .build());
    }

    public CompanyResponse createCompany(CreateCompanyRequest request) {
        Company company = Company.builder()
                .name(request.getName())
                .build();

        Company saved = companyRepository.save(company);

        return toResponse(saved);
    }

    public CompanyResponse getCompanyByIdForUser(UUID companyId, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (user.getRole() == Role.DEAN) {
            return toResponse(company);
        }

        if (user.getRole() == Role.MANAGER) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(companyId)) {
                throw new ForbiddenException("Access Denied");
            }
            return toResponse(company);
        }
        
        throw new ForbiddenException("Access Denied");
    }

    public CompanyResponse updateCompany(UUID companyId, UpdateCompanyRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        company.setName(request.getName());
        Company updated = companyRepository.save(company);

        return toResponse(updated);
    }

    public void deleteCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        companyRepository.delete(company);
    }

    private CompanyResponse toResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .build();
    }
}
