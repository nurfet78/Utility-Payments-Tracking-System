package org.nurfet.paymentstrackingsystem.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Данные зарегистрированного платежа.
 */
@Builder
@Schema(description = "Данные платежа по лицевому счёту")
public record PaymentResponse(

        @Schema(description = "Уникальный идентификатор платежа",
                example = "42")
        Long id,

        @Schema(description = "Сумма платежа",
                example = "1500.50")
        BigDecimal amount,

        @Schema(description = "Дата платежа в формате ISO-8601",
                example = "2025-01-15",
                type = "string",
                format = "date")
        LocalDate paymentDate
) {
}
