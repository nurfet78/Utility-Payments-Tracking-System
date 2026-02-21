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
import org.nurfet.paymentstrackingsystem.dto.request.MeterReadingCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.response.MeterReadingResponse;
import org.nurfet.paymentstrackingsystem.service.MeterReadingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Передача и просмотр показаний счётчиков.
 * Показания допустимы только для услуг с приборами учёта:
 * Газ (4 цифры), Вода (4 цифры), Электроэнергия (5 цифр).
 */
@RestController
@RequestMapping("/accounts/{accountId}/readings")
@RequiredArgsConstructor
@Tag(name = "Показания счётчиков",
        description = "Передача и просмотр показаний приборов учёта (Газ, Вода, Электроэнергия)")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Передать показание счётчика",
            description = """
                    Регистрирует показание прибора учёта для указанного лицевого счёта.
                    
                    Ограничения:
                    • Услуга должна поддерживать счётчик (hasMeter = true)
                    • Показание — строка из цифр строго фиксированной длины:
                      - Газ: ровно 4 цифры (например, "0523")
                      - Вода: ровно 4 цифры (например, "1047")
                      - Электроэнергия: ровно 5 цифр (например, "28319")
                    • Для услуг без счётчика (Домофон, Отопление и др.) операция запрещена"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Показание успешно зарегистрировано"),
            @ApiResponse(responseCode = "400",
                    description = """
                            Ошибка бизнес-логики:
                            • услуга не поддерживает счётчик
                            • неверная длина показания
                            • недопустимые символы""",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Лицевой счёт с указанным ID не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public MeterReadingResponse create(
            @Parameter(description = "ID лицевого счёта (услуга должна поддерживать счётчик)",
                    example = "1",
                    required = true)
            @PathVariable Long accountId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Показание счётчика и дата снятия",
                    required = true)
            @Valid @RequestBody MeterReadingCreateRequest request) {
        return meterReadingService.create(accountId, request);
    }

    @GetMapping
    @Operation(
            summary = "Получить историю показаний",
            description = "Возвращает все показания счётчика по лицевому счёту, отсортированные по дате (новые первыми)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "История показаний успешно получена"),
            @ApiResponse(responseCode = "404",
                    description = "Лицевой счёт с указанным ID не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<MeterReadingResponse> getByAccount(
            @Parameter(description = "ID лицевого счёта", example = "1", required = true)
            @PathVariable Long accountId) {
        return meterReadingService.getByAccountId(accountId);
    }

    @PutMapping("/{readingId}")
    @Operation(
            summary = "Редактировать показание",
            description = "Обновляет значение и/или дату существующего показания счётчика."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Показание успешно обновлено"),
            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Лицевой счёт или показание не найдено",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public MeterReadingResponse update(
            @Parameter(description = "ID лицевого счёта", example = "1", required = true)
            @PathVariable Long accountId,
            @Parameter(description = "ID показания", example = "1", required = true)
            @PathVariable Long readingId,
            @Valid @RequestBody MeterReadingCreateRequest request) {
        return meterReadingService.update(accountId, readingId, request);
    }

    @DeleteMapping("/{readingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить показание",
            description = "Удаляет показание счётчика по ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Показание удалено"),
            @ApiResponse(responseCode = "404",
                    description = "Лицевой счёт или показание не найдено",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(
            @Parameter(description = "ID лицевого счёта", example = "1", required = true)
            @PathVariable Long accountId,
            @Parameter(description = "ID показания", example = "1", required = true)
            @PathVariable Long readingId) {
        meterReadingService.delete(accountId, readingId);
    }
}
