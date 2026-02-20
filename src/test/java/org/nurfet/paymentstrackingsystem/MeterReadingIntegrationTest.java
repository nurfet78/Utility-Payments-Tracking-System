package org.nurfet.paymentstrackingsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nurfet.paymentstrackingsystem.dto.request.AccountCreateRequest;
import org.nurfet.paymentstrackingsystem.dto.request.MeterReadingCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MeterReadingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Создание показания для Газа (4 цифры) — успех")
    void shouldCreateMeterReadingForGas() throws Exception {
        Long gasAccountId = createAccount("GAS", "100-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("1234", LocalDate.of(2026, 2, 20));

        mockMvc.perform(post("/accounts/{id}/readings", gasAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.value").value("1234"))
                .andExpect(jsonPath("$.readingDate").value("2026-02-20"));
    }

    @Test
    @DisplayName("Создание показания для Воды (4 цифры) — успех")
    void shouldCreateMeterReadingForWater() throws Exception {
        Long waterAccountId = createAccount("WATER", "200-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("0056", LocalDate.of(2026, 2, 1));

        mockMvc.perform(post("/accounts/{id}/readings", waterAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").value("0056"));
    }

    @Test
    @DisplayName("Создание показания для Электроэнергии (5 цифр) — успех")
    void shouldCreateMeterReadingForElectricity() throws Exception {
        Long elAccountId = createAccount("ELECTRICITY", "300-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("12345", LocalDate.of(2026, 3, 1));

        mockMvc.perform(post("/accounts/{id}/readings", elAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").value("12345"));
    }

    @Test
    @DisplayName("Ошибка при неправильной длине показания для Газа (5 вместо 4)")
    void shouldRejectWrongLengthForGas() throws Exception {
        Long gasAccountId = createAccount("GAS", "101-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("12345", LocalDate.of(2026, 1, 20));

        mockMvc.perform(post("/accounts/{id}/readings", gasAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("ровно 4 цифр")));
    }

    @Test
    @DisplayName("Ошибка при неправильной длине показания для Электроэнергии (4 вместо 5)")
    void shouldRejectWrongLengthForElectricity() throws Exception {
        Long elAccountId = createAccount("ELECTRICITY", "301-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("1234", LocalDate.of(2026, 3, 1));

        mockMvc.perform(post("/accounts/{id}/readings", elAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("ровно 5 цифр")));
    }

    @Test
    @DisplayName("Ошибка при попытке создать показание для Домофона")
    void shouldRejectMeterReadingForIntercom() throws Exception {
        Long intercomAccountId = createAccount("INTERCOM", "400-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("1234", LocalDate.of(2026, 1, 20));

        mockMvc.perform(post("/accounts/{id}/readings", intercomAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("не поддерживает показания")));
    }

    @Test
    @DisplayName("Ошибка при попытке создать показание для Отопления")
    void shouldRejectMeterReadingForHeating() throws Exception {
        Long heatingAccountId = createAccount("HEATING", "500-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("1234", LocalDate.of(2026, 1, 20));

        mockMvc.perform(post("/accounts/{id}/readings", heatingAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("не поддерживает показания")));
    }

    @Test
    @DisplayName("Ошибка при попытке создать показание для Капитального ремонта")
    void shouldRejectMeterReadingForCapitalRepair() throws Exception {
        Long crAccountId = createAccount("CAPITAL_REPAIR", "600-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("1234", LocalDate.of(2026, 1, 20));

        mockMvc.perform(post("/accounts/{id}/readings", crAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("не поддерживает показания")));
    }

    @Test
    @DisplayName("Получение списка показаний по аккаунту")
    void shouldReturnReadingsHistory() throws Exception {
        Long gasAccountId = createAccount("GAS", "102-" + System.nanoTime());

        createReading(gasAccountId, "1111", "2026-01-01");
        createReading(gasAccountId, "2222", "2026-02-01");

        mockMvc.perform(get("/accounts/{id}/readings", gasAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].readingDate").value("2026-02-01"))
                .andExpect(jsonPath("$[1].readingDate").value("2026-01-01"));
    }

    @Test
    @DisplayName("Ошибка при нецифровых символах в показании")
    void shouldRejectNonDigitReading() throws Exception {
        Long gasAccountId = createAccount("GAS", "103-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("12ab", LocalDate.of(2026, 1, 20));

        mockMvc.perform(post("/accounts/{id}/readings", gasAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
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

    private void createReading(Long accId, String value, String date) throws Exception {
        var request = new MeterReadingCreateRequest(value, LocalDate.parse(date));
        mockMvc.perform(post("/accounts/{id}/readings", accId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
