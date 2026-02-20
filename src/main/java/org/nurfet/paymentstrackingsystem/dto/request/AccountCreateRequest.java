package org.nurfet.paymentstrackingsystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание лицевого счёта.
 * Номер счёта уникален в рамках конкретной услуги.
 */
@Schema(description = "Запрос на создание лицевого счёта")
public record AccountCreateRequest(

        @Schema(description = "Код типа услуги из справочника ServiceType",
                example = "GAS",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Тип услуги обязателен")
        String serviceType,

        @Schema(description = "Номер лицевого счёта. Допустимы цифры, точка, тире. Уникален в рамках услуги",
                example = "12345-01.7",
                maxLength = 30,
                pattern = "^[0-9.\\-]+$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Номер лицевого счёта обязателен")
        @Size(max = 30, message = "Максимальная длина номера — 30 символов")
        @Pattern(regexp = "^[0-9.\\-]+$", message = "Допустимые символы: цифры, точка, тире")
        String accountNumber
) {
}
