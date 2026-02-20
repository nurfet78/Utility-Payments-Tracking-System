package org.nurfet.paymentstrackingsystem.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Показание счётчика.
 * Допустимо только для услуг с hasMeter = true.
 * Значение хранится как строка фиксированной длины (важно количество цифр).
 */
@Entity
@Table(name = "meter_readings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 10)
    private String value;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;
}
