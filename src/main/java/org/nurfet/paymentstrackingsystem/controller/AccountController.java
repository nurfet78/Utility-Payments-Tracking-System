package org.nurfet.paymentstrackingsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nurfet.paymentstrackingsystem.dto.error.ErrorResponse;
import org.nurfet.paymentstrackingsystem.dto.request.AccountCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.response.AccountResponse;
import org.nurfet.paymentstrackingsystem.dto.response.AccountWithStatusResponse;
import org.nurfet.paymentstrackingsystem.service.AccountService;
import org.nurfet.paymentstrackingsystem.service.PaymentStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Управление лицевыми счетами.
 * Лицевой счёт привязывается к конкретной услуге.
 * Номер уникален в рамках одного типа услуги.
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Лицевые счета", description = "Создание и просмотр лицевых счетов, статус оплаты")
public class AccountController {

    private final AccountService accountService;
    private final PaymentStatusService paymentStatusService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать лицевой счёт",
            description = """
                    Создаёт новый лицевой счёт для указанной услуги.
                    Номер счёта должен быть уникален в рамках типа услуги.
                    Допустимые символы в номере: цифры, точка, тире."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Лицевой счёт успешно создан"),
            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации или дубликат счёта",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AccountResponse create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания лицевого счёта",
                    required = true)
            @Valid @RequestBody AccountCreateRequest request) {
        return accountService.create(request);
    }

    @GetMapping("/with-status")
    @Operation(
            summary = "Получить все счета со статусом оплаты",
            description = """
                    Возвращает все лицевые счета с динамически вычисленным статусом
                    оплаты за текущий месяц.
                    
                    Правила определения статуса:
                    • PAID_THIS_MONTH — есть хотя бы один платёж в текущем месяце
                    • NOT_PAID_THIS_MONTH — платежей нет, текущая дата ≤ 25
                    • OVERDUE — платежей нет, текущая дата > 25
                    
                    Выполняется за 2 SQL-запроса (без N+1)."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список счетов со статусами успешно получен"),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<AccountWithStatusResponse> getWithStatus() {
        return paymentStatusService.getAccountsWithStatus();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить лицевой счёт по ID",
            description = "Возвращает данные лицевого счёта по его уникальному идентификатору."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Лицевой счёт найден"),
            @ApiResponse(responseCode = "404",
                    description = "Лицевой счёт с указанным ID не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AccountResponse getById(
            @Parameter(description = "ID лицевого счёта", example = "1", required = true)
            @PathVariable Long id) {
        return accountService.getById(id);
    }
}
