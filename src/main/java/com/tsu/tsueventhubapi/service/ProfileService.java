package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.CompanyResponse;
import com.tsu.tsueventhubapi.dto.UserResponse;
import com.tsu.tsueventhubapi.enumeration.Role;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CompanyResponse companyResponse = null;
        if (user.getRole() == Role.MANAGER && user.getCompany() != null) {
            companyResponse = CompanyResponse.builder()
                    .id(user.getCompany().getId())
                    .name(user.getCompany().getName())
                    .build();
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .telegramId(user.getTelegramId())
                .company(companyResponse)
                .build();
    }
}
