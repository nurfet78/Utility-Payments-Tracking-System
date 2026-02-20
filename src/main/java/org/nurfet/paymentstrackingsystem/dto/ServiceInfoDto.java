package org.nurfet.paymentstrackingsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Информация о коммунальной услуге.
 * Возвращается в GET /services — фиксированный справочник из 8 услуг.
 */
@Builder
@Schema(description = "Информация о коммунальной услуге из фиксированного справочника")
public record ServiceInfoDto(

        @Schema(description = "Код услуги (значение enum ServiceType)",
                example = "GAS")
        String code,

        @Schema(description = "Отображаемое название услуги на русском языке",
                example = "Газ")
        String displayName,

        @Schema(description = "Наличие прибора учёта (счётчика) у данной услуги",
                example = "true")
        boolean hasMeter,

        @Schema(description = "Количество цифр в показании счётчика. null — если услуга без счётчика",
                example = "4",
                nullable = true)
        Integer meterDigits
) {
}
