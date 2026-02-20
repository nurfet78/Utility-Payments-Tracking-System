package org.nurfet.paymentstrackingsystem.mapper;

import org.nurfet.paymentstrackingsystem.domain.Account;
import org.nurfet.paymentstrackingsystem.domain.MeterReading;
import org.nurfet.paymentstrackingsystem.dto.request.MeterReadingCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.response.MeterReadingResponse;
import org.springframework.stereotype.Component;

@Component
public class MeterReadingMapper {

    public MeterReadingResponse toResponse(MeterReading reading) {
        return MeterReadingResponse.builder()
                .id(reading.getId())
                .value(reading.getValue())
                .readingDate(reading.getReadingDate())
                .build();
    }

    public MeterReading toEntity(Account account, MeterReadingCreateRequest request) {
        return MeterReading.builder()
                .account(account)
                .value(request.value())
                .readingDate(request.readingDate())
                .build();
    }
}
