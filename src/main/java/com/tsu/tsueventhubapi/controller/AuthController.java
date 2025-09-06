package com.tsu.tsueventhubapi.controller;

import com.tsu.tsueventhubapi.dto.JwtResponse;
import com.tsu.tsueventhubapi.dto.RegisterRequest;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.security.JwtTokenProvider;
import com.tsu.tsueventhubapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public JwtResponse register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        String token = jwtTokenProvider.generateToken(user);
        return new JwtResponse(token);
    }
}
