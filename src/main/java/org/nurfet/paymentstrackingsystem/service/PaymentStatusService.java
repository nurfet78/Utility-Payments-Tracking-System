package org.nurfet.paymentstrackingsystem.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.paymentstrackingsystem.domain.Account;
import org.nurfet.paymentstrackingsystem.domain.PaymentStatus;
import org.nurfet.paymentstrackingsystem.dto.response.AccountWithStatusResponse;
import org.nurfet.paymentstrackingsystem.repository.AccountRepository;
import org.nurfet.paymentstrackingsystem.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentStatusService {

    private static final int OVERDUE_DAY_THRESHOLD = 25;

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    /**
     * Вычисляет статус оплаты за текущий месяц для всех аккаунтов в системе.
     *
     * Алгоритм:
     * 1. Загружаем все аккаунты (один запрос).
     * 2. Одним агрегирующим запросом получаем ID аккаунтов с платежами за текущий месяц.
     * 3. В памяти вычисляем статус каждого аккаунта.
     *
     * Итого: ровно 2 запроса к БД, без N+1.
     */
    @Transactional(readOnly = true)
    public List<AccountWithStatusResponse> getAccountsWithStatus() {
        LocalDate today = LocalDate.now(clock);
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        List<Account> allAccounts = accountRepository.findAll();
        Set<Long> paidAccountIds = paymentRepository.findAccountIdsWithPaymentsBetween(startOfMonth, endOfMonth);

        return allAccounts.stream()
                .map(account -> toResponseWithStatus(account, paidAccountIds, today))
                .toList();
    }

    private AccountWithStatusResponse toResponseWithStatus(Account account, Set<Long> paidAccountIds, LocalDate today) {
        PaymentStatus status = computeStatus(account.getId(), paidAccountIds, today);

        return AccountWithStatusResponse.builder()
                .accountId(account.getId())
                .serviceType(account.getServiceType().name())
                .serviceDisplayName(account.getServiceType().getDisplayName())
                .accountNumber(account.getAccountNumber())
                .status(status.name())
                .statusDisplayName(status.getDisplayName())
                .build();
    }

    private PaymentStatus computeStatus(Long accountId, Set<Long> paidAccountIds, LocalDate today) {
        if (paidAccountIds.contains(accountId)) {
            return PaymentStatus.PAID_THIS_MONTH;
        }

        if (today.getDayOfMonth() > OVERDUE_DAY_THRESHOLD) {
            return PaymentStatus.OVERDUE;
        }

        return PaymentStatus.NOT_PAID_THIS_MONTH;
    }
}
