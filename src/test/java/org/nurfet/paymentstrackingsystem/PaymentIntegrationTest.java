package org.nurfet.paymentstrackingsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nurfet.paymentstrackingsystem.dto.request.AccountCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.request.PaymentCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long accountId;

    @BeforeEach
    void setUp() throws Exception {
        var accountRequest = new AccountCreateRequest("INTERCOM", "700-" + System.nanoTime());

        String response = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        accountId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @DisplayName("Создание платежа — успешный сценарий")
    void shouldCreatePayment() throws Exception {
        var request = new PaymentCreateRequest(
                new BigDecimal("1500.50"),
                LocalDate.of(2026, 1, 15)
        );

        mockMvc.perform(post("/accounts/{id}/payments", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.amount").value(1500.50))
                .andExpect(jsonPath("$.paymentDate").value("2026-01-15"));
    }

    @Test
    @DisplayName("Ошибка при отрицательной сумме")
    void shouldRejectNegativeAmount() throws Exception {
        var request = new PaymentCreateRequest(
                new BigDecimal("-100.00"),
                LocalDate.of(2026, 1, 15)
        );

        mockMvc.perform(post("/accounts/{id}/payments", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка при нулевой сумме")
    void shouldRejectZeroAmount() throws Exception {
        var request = new PaymentCreateRequest(
                BigDecimal.ZERO,
                LocalDate.of(2026, 1, 15)
        );

        mockMvc.perform(post("/accounts/{id}/payments", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("История платежей — сортировка по дате (desc)")
    void shouldReturnPaymentHistory() throws Exception {
        // Создаём три платежа
        createPayment(accountId, "500.00", "2026-01-10");
        createPayment(accountId, "600.00", "2026-02-10");
        createPayment(accountId, "700.00", "2026-03-10");

        mockMvc.perform(get("/accounts/{id}/payments", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].paymentDate").value("2026-03-10"))
                .andExpect(jsonPath("$[1].paymentDate").value("2026-02-10"))
                .andExpect(jsonPath("$[2].paymentDate").value("2026-01-10"));
    }

    @Test
    @DisplayName("Ошибка при платеже для несуществующего аккаунта")
    void shouldRejectPaymentForMissingAccount() throws Exception {
        var request = new PaymentCreateRequest(
                new BigDecimal("100.00"),
                LocalDate.of(2026, 1, 15)
        );

        mockMvc.perform(post("/accounts/{id}/payments", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    private void createPayment(Long accId, String amount, String date) throws Exception {
        var request = new PaymentCreateRequest(
                new BigDecimal(amount),
                LocalDate.parse(date)
        );
        mockMvc.perform(post("/accounts/{id}/payments", accId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
