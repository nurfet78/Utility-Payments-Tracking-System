package org.nurfet.paymentstrackingsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nurfet.paymentstrackingsystem.dto.request.AccountCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.request.PaymentCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(PaymentStatusIntegrationTest.TestClockConfig.class)
class PaymentStatusIntegrationTest extends AbstractIntegrationTest {

    /**
     * Фиксированные часы: 26 января 2026 (после 25-го — для проверки OVERDUE).
     */
    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 1, 26);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        public Clock testClock() {
            return FIXED_CLOCK;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("PAID_THIS_MONTH — аккаунт с платежом в текущем месяце")
    void shouldReturnPaidStatus() throws Exception {
        Long accountId = createAccount("GAS", "10001." + System.nanoTime());

        // Платёж в январе 2025 (текущий месяц по FIXED_CLOCK)
        createPayment(accountId, "500.00", "2026-01-15");

        mockMvc.perform(get("/accounts/with-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == %d)].status", accountId)
                        .value("PAID_THIS_MONTH"));
    }

    @Test
    @DisplayName("OVERDUE — нет платежа и дата > 25 числа")
    void shouldReturnOverdueStatus() throws Exception {
        // Создаём аккаунт БЕЗ платежа в январе 2025, дата = 26 января
        Long accountId = createAccount("WATER", "20001." + System.nanoTime());

        mockMvc.perform(get("/accounts/with-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == %d)].status", accountId)
                        .value("OVERDUE"));
    }

    @Test
    @DisplayName("OVERDUE не применяется, если есть платёж (даже после 25-го)")
    void shouldReturnPaidEvenAfter25th() throws Exception {
        Long accountId = createAccount("ELECTRICITY", "30001." + System.nanoTime());

        // Платёж 26 января — оплачено, несмотря на дату > 25
        createPayment(accountId, "300.00", "2026-01-26");

        mockMvc.perform(get("/accounts/with-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == %d)].status", accountId)
                        .value("PAID_THIS_MONTH"));
    }

    @Test
    @DisplayName("Платёж в прошлом месяце не считается — статус OVERDUE")
    void shouldNotCountPreviousMonthPayment() throws Exception {
        Long accountId = createAccount("INTERCOM", "40001." + System.nanoTime());

        // Платёж в декабре 2025 — не в текущем месяце
        createPayment(accountId, "200.00", "2025-12-20");

        mockMvc.perform(get("/accounts/with-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == %d)].status", accountId)
                        .value("OVERDUE"));
    }

    @Test
    @DisplayName("Все аккаунты присутствуют в ответе — независимо от типа услуги")
    void shouldReturnStatusForAllAccounts() throws Exception {
        Long gasId = createAccount("GAS", "50001." + System.nanoTime());
        Long heatingId = createAccount("HEATING", "60001." + System.nanoTime());
        Long ecoId = createAccount("ECO_RESOURCES", "70001." + System.nanoTime());
        Long repairId = createAccount("CAPITAL_REPAIR", "80001." + System.nanoTime());

        createPayment(gasId, "100.00", "2026-01-10");

        var result = mockMvc.perform(get("/accounts/with-status"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var tree = objectMapper.readTree(result);

        // Проверяем, что все 4 созданных аккаунта в ответе
        mockMvc.perform(get("/accounts/with-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == %d)]", gasId).exists())
                .andExpect(jsonPath("$[?(@.accountId == %d)]", heatingId).exists())
                .andExpect(jsonPath("$[?(@.accountId == %d)]", ecoId).exists())
                .andExpect(jsonPath("$[?(@.accountId == %d)]", repairId).exists())
                .andExpect(jsonPath("$[?(@.accountId == %d)].status", gasId).value("PAID_THIS_MONTH"))
                .andExpect(jsonPath("$[?(@.accountId == %d)].status", heatingId).value("OVERDUE"))
                .andExpect(jsonPath("$[?(@.accountId == %d)].status", ecoId).value("OVERDUE"))
                .andExpect(jsonPath("$[?(@.accountId == %d)].status", repairId).value("OVERDUE"));
    }

    @Test
    @DisplayName("Ответ содержит все необходимые поля")
    void shouldReturnAllRequiredFields() throws Exception {
        Long accountId = createAccount("HOUSING_SERVICE", "90001." + System.nanoTime());

        mockMvc.perform(get("/accounts/with-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == %d)].serviceType", accountId).value("HOUSING_SERVICE"))
                .andExpect(jsonPath("$[?(@.accountId == %d)].serviceDisplayName", accountId).value("Жилсервис"))
                .andExpect(jsonPath("$[?(@.accountId == %d)].accountNumber", accountId).isNotEmpty())
                .andExpect(jsonPath("$[?(@.accountId == %d)].status", accountId).isNotEmpty())
                .andExpect(jsonPath("$[?(@.accountId == %d)].statusDisplayName", accountId).isNotEmpty());
    }

    @Test
    @DisplayName("Пустой список, если нет аккаунтов (кроме созданных другими тестами)")
    void shouldReturnListEvenIfEmpty() throws Exception {
        // Просто проверяем, что endpoint не падает
        mockMvc.perform(get("/accounts/with-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private Long createAccount(String serviceType, String accountNumber) throws Exception {
        var request = new AccountCreateRequest(serviceType, accountNumber);
        String response = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void createPayment(Long accountId, String amount, String date) throws Exception {
        var request = new PaymentCreateRequest(
                new BigDecimal(amount),
                java.time.LocalDate.parse(date)
        );
        mockMvc.perform(post("/accounts/{id}/payments", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
