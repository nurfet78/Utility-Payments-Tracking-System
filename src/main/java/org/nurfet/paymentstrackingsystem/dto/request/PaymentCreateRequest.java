package org.nurfet.paymentstrackingsystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Запрос на регистрацию платежа по лицевому счёту.
 * Платёж не содержит показаний — это независимая сущность.
 */
@Schema(description = "Запрос на создание платежа по лицевому счёту")
public record PaymentCreateRequest(

        @Schema(description = "Сумма платежа. Строго больше нуля, два знака после запятой",
                example = "1500.50",
                minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Сумма оплаты обязательна")
        @DecimalMin(value = "0.01", message = "Сумма должна быть строго больше нуля")
        BigDecimal amount,

        @Schema(description = "Дата платежа в формате ISO-8601 (YYYY-MM-DD)",
                example = "2025-01-15",
                type = "string",
                format = "date",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Дата платежа обязательна")
        LocalDate paymentDate
) {
}
