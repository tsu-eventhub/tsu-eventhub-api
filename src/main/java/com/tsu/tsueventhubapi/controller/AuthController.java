package com.tsu.tsueventhubapi.controller;

import com.tsu.tsueventhubapi.dto.RefreshRequest;
import com.tsu.tsueventhubapi.dto.TokenResponse;
import com.tsu.tsueventhubapi.dto.RegisterRequest;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.UserRepository;
import com.tsu.tsueventhubapi.security.CustomUserDetailsService;
import com.tsu.tsueventhubapi.security.JwtTokenProvider;
import com.tsu.tsueventhubapi.service.AuthService;
import com.tsu.tsueventhubapi.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public TokenResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String email = refreshTokenService.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtTokenProvider.generateToken(
                userDetailsService.loadUserByUsername(user.getEmail())
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                userDetailsService.loadUserByUsername(user.getEmail())
        );

        refreshTokenService.deleteRefreshToken(refreshToken);
        refreshTokenService.storeRefreshToken(
                newRefreshToken,
                user.getEmail(),
                jwtTokenProvider.getRefreshExpirationMs()
        );

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

}
