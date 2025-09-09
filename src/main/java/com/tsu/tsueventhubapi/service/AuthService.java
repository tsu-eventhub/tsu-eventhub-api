package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.*;
import com.tsu.tsueventhubapi.enumeration.Role;
import com.tsu.tsueventhubapi.enumeration.Status;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.UserRepository;
import com.tsu.tsueventhubapi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuthenticationManager authenticationManager;

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

    public TokenResponse refresh(String refreshToken) {
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String email = refreshTokenService.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String newAccessToken = jwtTokenProvider.generateToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        refreshTokenService.deleteRefreshToken(refreshToken);
        refreshTokenService.storeRefreshToken(
                newRefreshToken,
                user.getEmail(),
                jwtTokenProvider.getRefreshExpirationMs()
        );

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public TokenResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            
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
