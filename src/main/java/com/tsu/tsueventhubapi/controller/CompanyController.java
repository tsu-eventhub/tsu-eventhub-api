package com.tsu.tsueventhubapi.controller;


import com.tsu.tsueventhubapi.dto.CompanyPageResponse;
import com.tsu.tsueventhubapi.dto.CompanyResponse;
import com.tsu.tsueventhubapi.dto.CreateCompanyRequest;
import com.tsu.tsueventhubapi.dto.UpdateCompanyRequest;
import com.tsu.tsueventhubapi.exception.ErrorResponse;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import com.tsu.tsueventhubapi.service.CompanyService;
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

import java.util.UUID;

@RestController
@RequestMapping("/companies")
@Tag(name = "Companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(
            summary = "Получение списка компаний",
            description = """
                Поддерживает два сценария:
                
                1. **Без авторизации (анонимный пользователь)** — возвращает список компаний для выбора на этапе регистрации менеджера.
                2. **Авторизованный пользователь**:
                   - Роль **DEAN** — получает полный список компаний.
                   - Роль **MANAGER** — если компания уже закреплена, возвращается пустой список; иначе — доступные компании для выбора.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список компаний успешно получен",
                    content = @Content(schema = @Schema(implementation = CompanyPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован (только если передан токен, но он недействителен)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён (менеджер или студент уже зарегистрированы)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Page<CompanyResponse>> getCompanies(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CompanyResponse> companies;

        if (currentUser != null) {
            companies = companyService.getCompaniesForUser(currentUser.getId(), page, size);
        } else {
            companies = companyService.getCompaniesForRegistration(page, size);
        }

        return ResponseEntity.ok(companies);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @ApprovedOnly
    @PreAuthorize("hasRole('DEAN')")
    @Operation(
            summary = "Создание компании",
            description = "Позволяет деканату создать новую компанию"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Компания успешно создана",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))
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
                    description = "Доступ запрещён (нужна роль DEAN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CompanyResponse> createCompany(
            @Valid @RequestBody CreateCompanyRequest request) {

        CompanyResponse company = companyService.createCompany(request);
        return ResponseEntity.status(200).body(company);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ApprovedOnly
    @PreAuthorize("hasRole('DEAN') or hasRole('MANAGER')")
    @Operation(
            summary = "Получение информации о компании по ID",
            description = "Позволяет получить данные конкретной компании. Доступно для DEAN и MANAGER (только если компания закреплена за ним)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о компании успешно получена",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён (например, MANAGER пытается получить чужую компанию)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Компания не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CompanyResponse> getCompanyById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        CompanyResponse company = companyService.getCompanyByIdForUser(id, currentUser);
        return ResponseEntity.ok(company);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ApprovedOnly
    @PreAuthorize("hasRole('DEAN')")
    @Operation(
            summary = "Редактирование компании",
            description = "Позволяет деканату изменить название компании по её ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Компания успешно обновлена",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))
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
                    description = "Доступ запрещён (требуется роль DEAN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Компания не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCompanyRequest request) {

        CompanyResponse updatedCompany = companyService.updateCompany(id, request);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ApprovedOnly
    @PreAuthorize("hasRole('DEAN')")
    @Operation(
            summary = "Удаление компании",
            description = "Позволяет деканату удалить компанию по ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Компания успешно удалена"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён (требуется роль DEAN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Компания не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok().build();
    }
}
