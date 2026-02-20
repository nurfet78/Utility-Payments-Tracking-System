package org.nurfet.paymentstrackingsystem.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.paymentstrackingsystem.domain.Account;
import org.nurfet.paymentstrackingsystem.domain.Payment;
import org.nurfet.paymentstrackingsystem.dto.request.PaymentCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.response.PaymentResponse;
import org.nurfet.paymentstrackingsystem.exception.BusinessException;
import org.nurfet.paymentstrackingsystem.mapper.PaymentMapper;
import org.nurfet.paymentstrackingsystem.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountService accountService;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse create(Long accountId, PaymentCreateRequest request) {
        Account account = accountService.findEntityById(accountId);

        validateAmount(request.amount());

        Payment payment = paymentMapper.toEntity(account, request);
        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByAccountId(Long accountId) {
        // Проверяем существование аккаунта
        accountService.findEntityById(accountId);

        return paymentRepository.findByAccountIdOrderByPaymentDateDesc(accountId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Сумма оплаты должна быть строго больше нуля");
        }
    }
}
