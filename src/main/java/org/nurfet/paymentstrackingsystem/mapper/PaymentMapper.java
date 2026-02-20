package org.nurfet.paymentstrackingsystem.mapper;

import org.nurfet.paymentstrackingsystem.domain.Account;
import org.nurfet.paymentstrackingsystem.domain.Payment;
import org.nurfet.paymentstrackingsystem.dto.request.PaymentCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.response.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .build();
    }

    public Payment toEntity(Account account, PaymentCreateRequest request) {
        return Payment.builder()
                .account(account)
                .amount(request.amount())
                .paymentDate(request.paymentDate())
                .build();
    }
}
