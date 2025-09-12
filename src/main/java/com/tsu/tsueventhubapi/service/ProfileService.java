package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.CompanyResponse;
import com.tsu.tsueventhubapi.dto.EventResponseSummary;
import com.tsu.tsueventhubapi.dto.UserResponse;
import com.tsu.tsueventhubapi.exception.ForbiddenException;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.Registration;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.RegistrationRepository;
import com.tsu.tsueventhubapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final ValidationService validationService;

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
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

    public Page<EventResponseSummary> getStudentEvents(UUID studentId, int page, int size) {
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Registration> registrations = registrationRepository.findByStudentIdAndUnregisteredAtIsNull(studentId, pageable);

        return registrations.map(reg -> {
            var event = reg.getEvent();
            return EventResponseSummary.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .startTime(event.getStartTime())
                    .location(event.getLocation())
                    .company(event.getCompany() != null
                            ? new CompanyResponse(event.getCompany().getId(), event.getCompany().getName())
                            : null)
                    .build();
        });
    }
}
