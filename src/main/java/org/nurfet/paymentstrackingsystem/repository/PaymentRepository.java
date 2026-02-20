package org.nurfet.paymentstrackingsystem.repository;

import org.nurfet.paymentstrackingsystem.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByAccountIdOrderByPaymentDateDesc(Long accountId);

    /**
     * Возвращает множество ID аккаунтов, для которых существует хотя бы один платёж
     * в заданном диапазоне дат [startInclusive, endInclusive].
     * Один агрегирующий запрос — без N+1.
     */
    @Query("SELECT DISTINCT p.account.id FROM Payment p " +
            "WHERE p.paymentDate >= :startDate AND p.paymentDate <= :endDate")
    Set<Long> findAccountIdsWithPaymentsBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
