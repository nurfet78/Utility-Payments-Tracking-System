package org.nurfet.paymentstrackingsystem.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Лицевой счёт — привязан к конкретной услуге.
 * Номер счёта уникален в рамках услуги (unique constraint на пару serviceType + accountNumber).
 */

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_account_service_number",
                columnNames = {"service_type", "account_number"}
        )
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(name = "account_number", nullable = false, length = 30)
    private String accountNumber;
}
