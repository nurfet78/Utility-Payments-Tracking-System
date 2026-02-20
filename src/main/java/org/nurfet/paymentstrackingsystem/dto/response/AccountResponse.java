package org.nurfet.paymentstrackingsystem.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Ответ с данными лицевого счёта.
 */
@Builder
@Schema(description = "Данные лицевого счёта")
public record AccountResponse(

        @Schema(description = "Уникальный идентификатор лицевого счёта в системе",
                example = "1")
        Long id,

        @Schema(description = "Код типа услуги",
                example = "GAS")
        String serviceType,

        @Schema(description = "Отображаемое название услуги на русском языке",
                example = "Газ")
        String serviceDisplayName,

        @Schema(description = "Номер лицевого счёта",
                example = "12345-01.7")
        String accountNumber
) {
}
