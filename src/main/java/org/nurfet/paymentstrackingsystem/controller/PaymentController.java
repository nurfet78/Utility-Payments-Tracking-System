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
import org.nurfet.paymentstrackingsystem.dto.request.PaymentCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.response.PaymentResponse;
import org.nurfet.paymentstrackingsystem.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Регистрация и просмотр платежей по лицевым счетам.
 * Платёж — независимая сущность, не содержит показаний счётчика.
 */
@RestController
@RequestMapping("/accounts/{accountId}/payments")
@RequiredArgsConstructor
@Tag(name = "Платежи", description = "Регистрация и просмотр платежей по лицевым счетам")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Зарегистрировать платёж",
            description = """
                    Создаёт новый платёж для указанного лицевого счёта.
                    Сумма должна быть строго больше нуля.
                    Дата платежа обязательна."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Платёж успешно зарегистрирован"),
            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации: отрицательная/нулевая сумма или отсутствует дата",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Лицевой счёт с указанным ID не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PaymentResponse create(
            @Parameter(description = "ID лицевого счёта", example = "1", required = true)
            @PathVariable Long accountId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные платежа: сумма и дата",
                    required = true)
            @Valid @RequestBody PaymentCreateRequest request) {
        return paymentService.create(accountId, request);
    }

    @GetMapping
    @Operation(
            summary = "Получить историю платежей",
            description = "Возвращает список всех платежей по лицевому счёту, отсортированных по дате (новые первыми)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "История платежей успешно получена"),
            @ApiResponse(responseCode = "404",
                    description = "Лицевой счёт с указанным ID не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<PaymentResponse> getByAccount(
            @Parameter(description = "ID лицевого счёта", example = "1", required = true)
            @PathVariable Long accountId) {
        return paymentService.getByAccountId(accountId);
    }
}
