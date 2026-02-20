package org.nurfet.paymentstrackingsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nurfet.paymentstrackingsystem.dto.request.AccountCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Создание лицевого счёта — успешный сценарий")
    void shouldCreateAccount() throws Exception {
        var request = new AccountCreateRequest("GAS", "123-45.67");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.serviceType").value("GAS"))
                .andExpect(jsonPath("$.serviceDisplayName").value("Газ"))
                .andExpect(jsonPath("$.accountNumber").value("123-45.67"));
    }

    @Test
    @DisplayName("Получение лицевого счёта по ID")
    void shouldGetAccountById() throws Exception {
        var request = new AccountCreateRequest("WATER", "001.002");

        String response = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/accounts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("001.002"))
                .andExpect(jsonPath("$.serviceType").value("WATER"));
    }

    @Test
    @DisplayName("Ошибка при создании дубликата — тот же номер и услуга")
    void shouldRejectDuplicateAccount() throws Exception {
        var request = new AccountCreateRequest("ELECTRICITY", "99999");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("уже существует")));
    }

    @Test
    @DisplayName("Ошибка при неизвестном типе услуги")
    void shouldRejectUnknownServiceType() throws Exception {
        var request = new AccountCreateRequest("INTERNET", "12345");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Неизвестный тип услуги")));
    }

    @Test
    @DisplayName("Ошибка при пустом номере счёта")
    void shouldRejectEmptyAccountNumber() throws Exception {
        var request = new AccountCreateRequest("GAS", "");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка при недопустимых символах в номере")
    void shouldRejectInvalidAccountNumber() throws Exception {
        var request = new AccountCreateRequest("GAS", "abc-123");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка 404 при несуществующем ID")
    void shouldReturn404ForMissingAccount() throws Exception {
        mockMvc.perform(get("/accounts/{id}", 99999))
                .andExpect(status().isNotFound());
    }
}
