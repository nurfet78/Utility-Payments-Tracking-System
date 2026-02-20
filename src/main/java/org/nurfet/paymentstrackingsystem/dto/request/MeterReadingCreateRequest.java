package org.nurfet.paymentstrackingsystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Запрос на передачу показания счётчика.
 * Допустимо только для услуг с hasMeter = true (Газ, Вода, Электроэнергия).
 * Длина показания строго фиксирована для каждой услуги.
 */
@Schema(description = "Запрос на передачу показания счётчика")
public record MeterReadingCreateRequest(

        @Schema(description = "Показание счётчика — строка из цифр фиксированной длины. "
                + "Газ и Вода: 4 цифры, Электроэнергия: 5 цифр",
                example = "1234",
                pattern = "^[0-9]+$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Показание счётчика обязательно")
        @Pattern(regexp = "^[0-9]+$", message = "Показание должно содержать только цифры")
        String value,

        @Schema(description = "Дата снятия показания в формате ISO-8601 (YYYY-MM-DD)",
                example = "2025-01-20",
                type = "string",
                format = "date",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Дата показания обязательна")
        LocalDate readingDate
) {
}
