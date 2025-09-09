package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.*;
import com.tsu.tsueventhubapi.enumeration.Status;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.UserRepository;
import com.tsu.tsueventhubapi.security.JwtTokenProvider;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import com.tsu.tsueventhubapi.security.UserDetailsImplService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsImplService userDetailsImplService;
    private final AuthenticationManager authenticationManager;

    public TokenResponse register(RegisterRequest request) {
        validationService.validateRegisterRequest(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .telegramUsername(request.getTelegramUsername())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(Status.REGISTERED)
                .build();

        User savedUser = userRepository.save(user);

        UserDetailsImpl userDetails = userDetailsImplService.loadUserById(savedUser.getId());

        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        refreshTokenService.storeRefreshToken(
                refreshToken,
                savedUser.getId().toString(),
                jwtTokenProvider.getRefreshExpirationMs()
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String userIdStr = refreshTokenService.getIdFromToken(refreshToken);
        UUID userId = UUID.fromString(userIdStr);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserDetailsImpl userDetails = userDetailsImplService.loadUserById(user.getId());

        String newAccessToken = jwtTokenProvider.generateToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        refreshTokenService.deleteRefreshToken(refreshToken);
        refreshTokenService.storeRefreshToken(
                newRefreshToken,
                user.getId().toString(),
                jwtTokenProvider.getRefreshExpirationMs()
        );

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public TokenResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            String accessToken = jwtTokenProvider.generateToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
            
            refreshTokenService.storeRefreshToken(
                    refreshToken,
                    request.getEmail(),
                    jwtTokenProvider.getRefreshExpirationMs()
            );

            return new TokenResponse(accessToken, refreshToken);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
