package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.RegisterRequest;
import com.tsu.tsueventhubapi.dto.TokenResponse;
import com.tsu.tsueventhubapi.enumeration.Status;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.UserRepository;
import com.tsu.tsueventhubapi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;

    public TokenResponse register(RegisterRequest request) {
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

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());

        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        refreshTokenService.storeRefreshToken(
                refreshToken,
                savedUser.getEmail(),
                jwtTokenProvider.getRefreshExpirationMs()
        );

        return new TokenResponse(accessToken, refreshToken);
    }
}
