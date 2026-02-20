package org.nurfet.paymentstrackingsystem.domain;

import lombok.Getter;

/**
 * Фиксированный набор коммунальных услуг.
 * Добавление новых услуг запрещено доменными правилами.
 */

@Getter
public enum ServiceType {

    GAS("Газ", true, 4),
    WATER("Вода", true, 4),
    ELECTRICITY("Электроэнергия", true, 5),
    INTERCOM("Домофон", false, null),
    HEATING("Отопление", false, null),
    ECO_RESOURCES("Экоресурсы", false, null),
    HOUSING_SERVICE("Жилсервис", false, null),
    CAPITAL_REPAIR("Капитальный ремонт", false, null);

    private final String displayName;
    private final boolean hasMeter;
    private final Integer meterDigits;

    ServiceType(String displayName, boolean hasMeter, Integer meterDigits) {
        this.displayName = displayName;
        this.hasMeter = hasMeter;
        this.meterDigits = meterDigits;
    }
}
