package com.tsu.tsueventhubapi.controller;

import com.tsu.tsueventhubapi.dto.CreateEventRequest;
import com.tsu.tsueventhubapi.dto.EventResponseFull;
import com.tsu.tsueventhubapi.dto.EventResponseSummary;
import com.tsu.tsueventhubapi.dto.UpdateEventRequest;
import com.tsu.tsueventhubapi.exception.ErrorResponse;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import com.tsu.tsueventhubapi.service.EventService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@Tag(name = "Events")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Получение списка событий",
            description = """
                Доступно для всех авторизованных пользователей.
                
                Возвращает полный список событий, созданных компаниями или менеджерами.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список событий успешно получен",
                    content = @Content(schema = @Schema(implementation = EventResponseSummary.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<EventResponseSummary>> getAllEvents(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(eventService.getAllEvents(currentUser.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Создание события",
            description = "Доступно только для менеджеров. Поддерживает вызовы через TelegramBot (по токену авторизованного менеджера)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Событие успешно создано",
                    content = @Content(schema = @Schema(implementation = EventResponseFull.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EventResponseFull> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        EventResponseFull response = eventService.createEvent(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Детали события",
            description = """
            Позволяет получить полную информацию о конкретном событии по его ID.
            Менеджеры видят только события своей компании.
            Остальные авторизованные пользователи видят все события.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о событии успешно получена",
                    content = @Content(schema = @Schema(implementation = EventResponseFull.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён (менеджер пытается получить событие чужой компании)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Событие не найдено",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EventResponseFull> getEventById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        EventResponseFull event = eventService.getEventById(id, currentUser.getId());
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Обновление события",
            description = """
            Доступно только для менеджеров.
            Поддерживает вызовы через TelegramBot (по токену авторизованного менеджера).
            Обновляет данные события, принадлежащего компании менеджера.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Событие успешно обновлено",
                    content = @Content(schema = @Schema(implementation = EventResponseFull.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён (менеджер пытается получить событие чужой компании)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Событие не найдено",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EventResponseFull> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        EventResponseFull response = eventService.updateEvent(id, currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удаление события", description = "Удаление события менеджером (через TelegramBot)")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Событие успешно удалено"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Нет прав на удаление события", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Событие не найдено", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID id, 
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        eventService.deleteEvent(id,  currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
