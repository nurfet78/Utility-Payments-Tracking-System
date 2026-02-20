package org.nurfet.paymentstrackingsystem.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Унифицированный ответ при ошибке.
 * Используется GlobalExceptionHandler для всех типов ошибок (400, 404, 500).
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Ответ при ошибке — содержит код, описание и опционально ошибки по полям")
public record ErrorResponse(

        @Schema(description = "HTTP-код ответа",
                example = "400")
        int status,

        @Schema(description = "Тип ошибки",
                example = "Business Rule Violation")
        String error,

        @Schema(description = "Человекочитаемое описание ошибки",
                example = "Лицевой счёт '12345' уже существует для услуги 'Газ'")
        String message,

        @Schema(description = "Ошибки валидации по отдельным полям (только для 400 Validation Failed)",
                example = """
                        {"accountNumber": "Допустимые символы: цифры, точка, тире"}""",
                nullable = true)
        Map<String, String> fieldErrors,

        @Schema(description = "Время возникновения ошибки",
                example = "2025-01-15T14:30:00",
                type = "string",
                format = "date-time")
        LocalDateTime timestamp
) {
}
