package com.tsu.tsueventhubapi.controller;

import com.tsu.tsueventhubapi.dto.UpdateProfileRequest;
import com.tsu.tsueventhubapi.dto.UserResponse;
import com.tsu.tsueventhubapi.exception.ErrorResponse;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import com.tsu.tsueventhubapi.service.ProfileService;
import com.tsu.tsueventhubapi.util.ApprovedOnly;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(
            summary = "Получение данных текущего пользователя", 
            description = "Возвращает профиль авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные пользователя успешно получены",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public UserResponse getProfile(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return profileService.getCurrentUser(currentUser.getId());
    }

    @PutMapping
    @ApprovedOnly
    @Operation(
            summary = "Редактирование профиля пользователя",
            description = "Позволяет изменить имя, email и Telegram ID текущего пользователя. "
                    + "Редактирование доступно только для подтверждённых аккаунтов."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлён",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации данных (например, неверный формат Telegram ID или email)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "403",
                    description = "Редактирование запрещено (например, аккаунт не подтверждён)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {

        User updatedUser = profileService.updateProfile(
                currentUser.getId(),
                request.getName(),
                request.getEmail(),
                request.getTelegramUsername()
        );

        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }
    
}
