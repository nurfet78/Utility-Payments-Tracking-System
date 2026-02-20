package org.nurfet.paymentstrackingsystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nurfet.paymentstrackingsystem.domain.Account;
import org.nurfet.paymentstrackingsystem.domain.ServiceType;
import org.nurfet.paymentstrackingsystem.dto.response.AccountWithStatusResponse;
import org.nurfet.paymentstrackingsystem.repository.AccountRepository;
import org.nurfet.paymentstrackingsystem.repository.PaymentRepository;
import org.nurfet.paymentstrackingsystem.service.PaymentStatusService;

import java.time.*;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentStatusServiceUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private PaymentStatusService paymentStatusService;

    @Test
    @DisplayName("NOT_PAID_THIS_MONTH — нет платежа, дата <= 25")
    void shouldReturnNotPaidBeforeDeadline() {
        // 20 марта 2026 — до дедлайна, без платежа
        fixClock(LocalDate.of(2026, 3, 20));

        Account account = buildAccount(1L, ServiceType.GAS, "111-222");
        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(paymentRepository.findAccountIdsWithPaymentsBetween(any(), any()))
                .thenReturn(Set.of());

        List<AccountWithStatusResponse> result = paymentStatusService.getAccountsWithStatus();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("NOT_PAID_THIS_MONTH");
    }

    @Test
    @DisplayName("OVERDUE — нет платежа, дата > 25")
    void shouldReturnOverdueAfterDeadline() {
        fixClock(LocalDate.of(2026, 3, 26));

        Account account = buildAccount(1L, ServiceType.WATER, "333-444");
        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(paymentRepository.findAccountIdsWithPaymentsBetween(any(), any()))
                .thenReturn(Set.of());

        List<AccountWithStatusResponse> result = paymentStatusService.getAccountsWithStatus();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("OVERDUE");
    }

    @Test
    @DisplayName("PAID_THIS_MONTH — есть платёж в текущем месяце")
    void shouldReturnPaidWhenPaymentExists() {
        fixClock(LocalDate.of(2026, 3, 26));

        Account account = buildAccount(1L, ServiceType.INTERCOM, "555-666");
        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(paymentRepository.findAccountIdsWithPaymentsBetween(any(), any()))
                .thenReturn(Set.of(1L));

        List<AccountWithStatusResponse> result = paymentStatusService.getAccountsWithStatus();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("PAID_THIS_MONTH");
    }

    @Test
    @DisplayName("Граничный случай — ровно 25 число, NOT_PAID (не OVERDUE)")
    void shouldReturnNotPaidOnExactly25th() {
        fixClock(LocalDate.of(2026, 3, 25));

        Account account = buildAccount(1L, ServiceType.HEATING, "777-888");
        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(paymentRepository.findAccountIdsWithPaymentsBetween(any(), any()))
                .thenReturn(Set.of());

        List<AccountWithStatusResponse> result = paymentStatusService.getAccountsWithStatus();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("NOT_PAID_THIS_MONTH");
    }

    @Test
    @DisplayName("Несколько аккаунтов с разными статусами")
    void shouldComputeStatusForMultipleAccounts() {
        fixClock(LocalDate.of(2026, 3, 28));

        Account paid = buildAccount(1L, ServiceType.GAS, "aaa");
        Account overdue = buildAccount(2L, ServiceType.WATER, "bbb");

        when(accountRepository.findAll()).thenReturn(List.of(paid, overdue));
        when(paymentRepository.findAccountIdsWithPaymentsBetween(any(), any()))
                .thenReturn(Set.of(1L)); // только первый оплатил

        List<AccountWithStatusResponse> result = paymentStatusService.getAccountsWithStatus();

        assertThat(result).hasSize(2);
        assertThat(result.stream().filter(r -> r.accountId().equals(1L)).findFirst().get().status())
                .isEqualTo("PAID_THIS_MONTH");
        assertThat(result.stream().filter(r -> r.accountId().equals(2L)).findFirst().get().status())
                .isEqualTo("OVERDUE");
    }

    private void fixClock(LocalDate date) {
        Clock fixedClock = Clock.fixed(
                date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    private Account buildAccount(Long id, ServiceType type, String number) {
        return Account.builder()
                .id(id)
                .serviceType(type)
                .accountNumber(number)
                .build();
    }
}
