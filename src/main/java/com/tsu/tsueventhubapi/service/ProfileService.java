package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.UserResponse;
import com.tsu.tsueventhubapi.exception.ForbiddenException;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ValidationService validationService;

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public User updateProfile(UUID userId, String newName, String newEmail, String telegramUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isApproved()) {
            throw new ForbiddenException("Profile editing is allowed only for approved accounts");
        }

        validationService.validateProfileUpdate(user.getRole(), telegramUsername);

        user.setName(newName);
        user.setEmail(newEmail);
        user.setTelegramUsername(telegramUsername);

        return userRepository.save(user);
    }
}
