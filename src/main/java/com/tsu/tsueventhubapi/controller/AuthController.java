package com.tsu.tsueventhubapi.controller;

import com.tsu.tsueventhubapi.dto.*;
import com.tsu.tsueventhubapi.exception.ErrorResponse;
import com.tsu.tsueventhubapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создает нового пользователя и возвращает токены")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная регистрация",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или пользователь уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление токенов", description = "Принимает refreshToken и возвращает новый access и refresh токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токены успешно обновлены",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный или просроченный refreshToken",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя", description = "Проверяет учетные данные и возвращает токены")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный email или пароль",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Получение текущего пользователя", 
            description = "Возвращает данные текущего авторизованного пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные пользователя успешно получены",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserResponse getMe() {
        return authService.getCurrentUser();
    }

}
