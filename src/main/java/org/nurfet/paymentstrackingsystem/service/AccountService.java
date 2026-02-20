package org.nurfet.paymentstrackingsystem.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.paymentstrackingsystem.domain.Account;
import org.nurfet.paymentstrackingsystem.domain.ServiceType;
import org.nurfet.paymentstrackingsystem.dto.request.AccountCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.response.AccountResponse;
import org.nurfet.paymentstrackingsystem.exception.BusinessException;
import org.nurfet.paymentstrackingsystem.exception.ResourceNotFoundException;
import org.nurfet.paymentstrackingsystem.mapper.AccountMapper;
import org.nurfet.paymentstrackingsystem.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse create(AccountCreateRequest request) {
        ServiceType serviceType = parseServiceType(request.serviceType());

        if (accountRepository.existsByServiceTypeAndAccountNumber(serviceType, request.accountNumber())) {
            throw new BusinessException(
                    "Лицевой счёт '%s' уже существует для услуги '%s'"
                            .formatted(request.accountNumber(), serviceType.getDisplayName())
            );
        }

        Account account = accountMapper.toEntity(serviceType, request.accountNumber());
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        Account account = findAccountOrThrow(id);
        return accountMapper.toResponse(account);
    }

    /**
     * Внутренний метод для получения сущности (используется другими сервисами).
     */
    @Transactional(readOnly = true)
    public Account findEntityById(Long id) {
        return findAccountOrThrow(id);
    }

    private Account findAccountOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Лицевой счёт с id=%d не найден".formatted(id)
                ));
    }

    private ServiceType parseServiceType(String value) {
        try {
            return ServiceType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Неизвестный тип услуги: '%s'".formatted(value));
        }
    }
}
