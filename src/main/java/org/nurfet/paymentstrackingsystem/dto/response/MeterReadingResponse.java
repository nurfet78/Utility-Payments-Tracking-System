package org.nurfet.paymentstrackingsystem.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Данные переданного показания счётчика.
 */
@Builder
@Schema(description = "Данные показания счётчика")
public record MeterReadingResponse(

        @Schema(description = "Уникальный идентификатор показания",
                example = "7")
        Long id,

        @Schema(description = "Значение показания (строка из цифр)",
                example = "1234")
        String value,

        @Schema(description = "Дата снятия показания в формате ISO-8601",
                example = "2025-01-20",
                type = "string",
                format = "date")
        LocalDate readingDate
) {
}
