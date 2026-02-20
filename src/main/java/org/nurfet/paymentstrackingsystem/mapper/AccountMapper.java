package org.nurfet.paymentstrackingsystem.mapper;

import org.nurfet.paymentstrackingsystem.domain.Account;
import org.nurfet.paymentstrackingsystem.domain.ServiceType;
import org.nurfet.paymentstrackingsystem.dto.response.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .serviceType(account.getServiceType().name())
                .serviceDisplayName(account.getServiceType().getDisplayName())
                .accountNumber(account.getAccountNumber())
                .build();
    }

    public Account toEntity(ServiceType serviceType, String accountNumber) {
        return Account.builder()
                .serviceType(serviceType)
                .accountNumber(accountNumber)
                .build();
    }
}
