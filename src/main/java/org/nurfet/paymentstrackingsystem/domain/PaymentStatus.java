package org.nurfet.paymentstrackingsystem.domain;

import lombok.Getter;

/**
 * Вычисляемый статус оплаты за текущий месяц.
 * Не хранится в БД — определяется динамически в сервисном слое.
 */

@Getter
public enum PaymentStatus {

    PAID_THIS_MONTH("Оплачено в текущем месяце"),
    NOT_PAID_THIS_MONTH("Не оплачено в текущем месяце"),
    OVERDUE("Просрочено");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}
