package org.nurfet.paymentstrackingsystem.repository;

import org.nurfet.paymentstrackingsystem.domain.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    List<MeterReading> findByAccountIdOrderByReadingDateDesc(Long accountId);
}
