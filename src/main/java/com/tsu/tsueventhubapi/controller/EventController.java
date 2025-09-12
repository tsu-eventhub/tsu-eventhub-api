package com.tsu.tsueventhubapi.controller;

import com.tsu.tsueventhubapi.dto.*;
import com.tsu.tsueventhubapi.exception.ErrorResponse;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import com.tsu.tsueventhubapi.service.EventService;
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
import org.springframework.data.domain.Page;
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
@ApprovedOnly
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
                    content = @Content(schema = @Schema(implementation = EventPageResponseSummary.class))
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
    public ResponseEntity<Page<EventResponseSummary>> getAllEvents(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EventResponseSummary> events = eventService.getAllEvents(currentUser.getId(), page, size);
        return ResponseEntity.ok(events);
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
                    description = "Доступ запрещён (например, менеджер пытается получить событие чужой компании)",
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
                    description = "Доступ запрещён (например, менеджер пытается получить событие чужой компании)",
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

    @Operation(
            summary = "Удаление события", 
            description = "Удаление события менеджером (через TelegramBot)")
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
        eventService.deleteEvent(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('MANAGER', 'DEAN')")
    @Operation(
            summary = "Список студентов события",
            description = """
            Позволяет получить список студентов, зарегистрированных на событие.
            
            - Менеджеры могут просматривать студентов только для событий, которые они создали.
            - Деканат может просматривать студентов для любых событий.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список студентов успешно получен",
                    content = @Content(schema = @Schema(implementation = StudentPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён (например, менеджер пытается получить студентов чужого события)",
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
    public ResponseEntity<Page<StudentResponse>> getStudents(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getStudentsForEvent(id, currentUser.getId(), page, size));
    }

    @PostMapping("/{id}/register")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Записаться на событие",
            description = """
            Позволяет студенту зарегистрироваться на конкретное событие.

            Правила:
            - Студент может зарегистрироваться только один раз на каждое событие.
            - Регистрация невозможна после истечения срока регистрации.
            - Регистрация невозможна, если событие уже завершилось.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Регистрация успешна"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации (например, уже зарегистрирован, срок регистрации истёк)",
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
    public ResponseEntity<Void> registerForEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        eventService.registerStudent(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unregister")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Отменить запись на событие",
            description = """
            Позволяет студенту отменить свою регистрацию на конкретное событие.

            Правила:
            - Студент может отменить запись только если он ранее зарегистрировался.
            - Отмена невозможна, если событие уже началось или завершилось.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Регистрация успешно отменена"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации (например, студент не зарегистрирован или событие уже началось)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
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
    public ResponseEntity<Void> unregisterFromEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        eventService.unregisterStudent(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
