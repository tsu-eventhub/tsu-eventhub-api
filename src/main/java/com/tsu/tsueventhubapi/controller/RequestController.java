package com.tsu.tsueventhubapi.controller;

import com.tsu.tsueventhubapi.dto.PendingUserResponse;
import com.tsu.tsueventhubapi.dto.RejectRequestDto;
import com.tsu.tsueventhubapi.exception.ErrorResponse;
import com.tsu.tsueventhubapi.service.RequestService;
import com.tsu.tsueventhubapi.util.ApprovedOnly;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/requests")
@Tag(name = "Requests")
@SecurityRequirement(name = "bearerAuth")
@ApprovedOnly
@RestController
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('DEAN','MANAGER')")
    @Operation(
            summary = "Список неподтверждённых заявок",
            description = "Возвращает список пользователей с неподтверждёнными заявками на одобрение. "
                    + "Доступно для роли DEAN или MANAGER (MANAGER видит только заявки внутри своей компании)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список неподтверждённых пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = PendingUserResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён: роль или статус пользователя не позволяет получить данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<PendingUserResponse> getPendingUsers() {
        return requestService.getPendingUsers();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('DEAN','MANAGER')")
    @Operation(
            summary = "Подтверждение заявки пользователя",
            description = "Позволяет деканату или подтверждённому менеджеру одобрить заявку пользователя. "
                    + "Менеджер с PENDING статусом не может одобрять себя."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заявка пользователя успешно одобрена"),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403",
                    description = "Доступ запрещён (например, менеджер с PENDING статусом пытается одобрить себя)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> approveUser(@PathVariable("id") UUID requestId) {
        requestService.approveUser(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('DEAN','MANAGER')")
    @Operation(
            summary = "Отклонение заявки пользователя",
            description = "Позволяет деканату или подтверждённому менеджеру отклонить заявку пользователя с указанием причины."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заявка пользователя успешно отклонена"),
            @ApiResponse(responseCode = "400", description = "Заявка пользователя уже была обработана",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403",
                    description = "Доступ запрещён (например, менеджер с PENDING статусом пытается отклонить себя)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> rejectUser(@PathVariable("id") UUID requestId,
                                           @RequestBody(required = false) RejectRequestDto requestDto) {
        String reason = requestDto != null ? requestDto.getReason() : null;
        requestService.rejectUser(requestId, reason);
        return ResponseEntity.ok().build();
    }
}
