package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.RegisterRequest;
import com.tsu.tsueventhubapi.enumeration.Status;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidationService validationService;

    public User register(RegisterRequest request) {
        validationService.validateRegisterRequest(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .telegramId(request.getTelegramId())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(Status.REGISTERED)
                .build();

        return userRepository.save(user);
    }
}
