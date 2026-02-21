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

class PaymentUpdateDeleteIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long accountId;
    private Long paymentId;

    @BeforeEach
    void setUp() throws Exception {
        accountId = createAccount("GAS", "800-" + System.nanoTime());
        paymentId = createPaymentAndGetId(accountId, "500.00", "2025-01-15");
    }

    // ======================== UPDATE ========================

    @Test
    @DisplayName("PUT — успешное редактирование суммы и даты платежа")
    void shouldUpdatePayment() throws Exception {
        var request = new PaymentCreateRequest(
                new BigDecimal("999.99"),
                LocalDate.of(2025, 2, 20)
        );

        mockMvc.perform(put("/accounts/{accId}/payments/{payId}", accountId, paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.amount").value(999.99))
                .andExpect(jsonPath("$.paymentDate").value("2025-02-20"));
    }

    @Test
    @DisplayName("PUT — обновлённый платёж отражается в истории")
    void shouldReflectUpdateInHistory() throws Exception {
        var request = new PaymentCreateRequest(
                new BigDecimal("777.00"),
                LocalDate.of(2025, 3, 01)
        );

        mockMvc.perform(put("/accounts/{accId}/payments/{payId}", accountId, paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/accounts/{id}/payments", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(777.00))
                .andExpect(jsonPath("$[0].paymentDate").value("2025-03-01"));
    }

    @Test
    @DisplayName("PUT — ошибка при отрицательной сумме")
    void shouldRejectUpdateWithNegativeAmount() throws Exception {
        var request = new PaymentCreateRequest(
                new BigDecimal("-50.00"),
                LocalDate.of(2025, 1, 15)
        );

        mockMvc.perform(put("/accounts/{accId}/payments/{payId}", accountId, paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT — ошибка при нулевой сумме")
    void shouldRejectUpdateWithZeroAmount() throws Exception {
        var request = new PaymentCreateRequest(
                BigDecimal.ZERO,
                LocalDate.of(2025, 1, 15)
        );

        mockMvc.perform(put("/accounts/{accId}/payments/{payId}", accountId, paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT — 404 при несуществующем платеже")
    void shouldReturn404ForNonExistentPayment() throws Exception {
        var request = new PaymentCreateRequest(
                new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 15)
        );

        mockMvc.perform(put("/accounts/{accId}/payments/{payId}", accountId, 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT — ошибка при редактировании чужого платежа")
    void shouldRejectUpdateOfPaymentFromAnotherAccount() throws Exception {
        Long otherAccountId = createAccount("WATER", "801-" + System.nanoTime());

        var request = new PaymentCreateRequest(
                new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 15)
        );

        // paymentId принадлежит accountId, пытаемся обновить через otherAccountId
        mockMvc.perform(put("/accounts/{accId}/payments/{payId}", otherAccountId, paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("не принадлежит")));
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE — успешное удаление платежа")
    void shouldDeletePayment() throws Exception {
        mockMvc.perform(delete("/accounts/{accId}/payments/{payId}", accountId, paymentId))
                .andExpect(status().isNoContent());

        // Проверяем что платёж удалён
        mockMvc.perform(get("/accounts/{id}/payments", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("DELETE — 404 при несуществующем платеже")
    void shouldReturn404WhenDeletingNonExistentPayment() throws Exception {
        mockMvc.perform(delete("/accounts/{accId}/payments/{payId}", accountId, 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE — ошибка при удалении чужого платежа")
    void shouldRejectDeleteOfPaymentFromAnotherAccount() throws Exception {
        Long otherAccountId = createAccount("ELECTRICITY", "802-" + System.nanoTime());

        mockMvc.perform(delete("/accounts/{accId}/payments/{payId}", otherAccountId, paymentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("не принадлежит")));
    }

    @Test
    @DisplayName("DELETE — повторное удаление того же платежа возвращает 404")
    void shouldReturn404OnDoubleDelete() throws Exception {
        mockMvc.perform(delete("/accounts/{accId}/payments/{payId}", accountId, paymentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/accounts/{accId}/payments/{payId}", accountId, paymentId))
                .andExpect(status().isNotFound());
    }

    // ======================== Helpers ========================

    private Long createAccount(String serviceType, String accountNumber) throws Exception {
        var request = new AccountCreateRequest(serviceType, accountNumber);
        String response = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createPaymentAndGetId(Long accId, String amount, String date) throws Exception {
        var request = new PaymentCreateRequest(new BigDecimal(amount), LocalDate.parse(date));
        String response = mockMvc.perform(post("/accounts/{id}/payments", accId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
