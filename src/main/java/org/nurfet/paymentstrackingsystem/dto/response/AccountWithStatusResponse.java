package org.nurfet.paymentstrackingsystem.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Лицевой счёт с динамически вычисленным статусом оплаты за текущий месяц.
 * Статус не хранится в БД — определяется в сервисном слое.
 */
@Builder
@Schema(description = "Лицевой счёт с вычисленным статусом оплаты за текущий месяц")
public record AccountWithStatusResponse(

        @Schema(description = "Уникальный идентификатор лицевого счёта",
                example = "1")
        Long accountId,

        @Schema(description = "Код типа услуги",
                example = "GAS")
        String serviceType,

        @Schema(description = "Отображаемое название услуги на русском языке",
                example = "Газ")
        String serviceDisplayName,

        @Schema(description = "Номер лицевого счёта",
                example = "12345-01.7")
        String accountNumber,

        @Schema(description = "Статус оплаты: PAID_THIS_MONTH, NOT_PAID_THIS_MONTH, OVERDUE",
                example = "PAID_THIS_MONTH",
                allowableValues = {"PAID_THIS_MONTH", "NOT_PAID_THIS_MONTH", "OVERDUE"})
        String status,

        @Schema(description = "Отображаемое название статуса на русском языке",
                example = "Оплачено в текущем месяце")
        String statusDisplayName
) {
}
