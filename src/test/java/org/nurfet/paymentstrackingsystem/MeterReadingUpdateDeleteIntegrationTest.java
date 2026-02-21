package org.nurfet.paymentstrackingsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

class MeterReadingUpdateDeleteIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long gasAccountId;   // Газ: 4 цифры
    private Long readingId;

    @BeforeEach
    void setUp() throws Exception {
        gasAccountId = createAccount("GAS", "900-" + System.nanoTime());
        readingId = createReadingAndGetId(gasAccountId, "0523", "2025-01-18");
    }

    // ======================== UPDATE ========================

    @Test
    @DisplayName("PUT — успешное редактирование показания")
    void shouldUpdateReading() throws Exception {
        var request = new MeterReadingCreateRequest("0777", LocalDate.of(2025, 2, 10));

        mockMvc.perform(put("/accounts/{accId}/readings/{rId}", gasAccountId, readingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(readingId))
                .andExpect(jsonPath("$.value").value("0777"))
                .andExpect(jsonPath("$.readingDate").value("2025-02-10"));
    }

    @Test
    @DisplayName("PUT — обновлённое показание отражается в истории")
    void shouldReflectUpdateInHistory() throws Exception {
        var request = new MeterReadingCreateRequest("0999", LocalDate.of(2025, 3, 01));

        mockMvc.perform(put("/accounts/{accId}/readings/{rId}", gasAccountId, readingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/accounts/{id}/readings", gasAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].value").value("0999"))
                .andExpect(jsonPath("$[0].readingDate").value("2025-03-01"));
    }

    @Test
    @DisplayName("PUT — ошибка при неправильной длине показания (5 вместо 4 для Газа)")
    void shouldRejectUpdateWithWrongLength() throws Exception {
        var request = new MeterReadingCreateRequest("12345", LocalDate.of(2025, 1, 20));

        mockMvc.perform(put("/accounts/{accId}/readings/{rId}", gasAccountId, readingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("ровно 4 цифр")));
    }

    @Test
    @DisplayName("PUT — ошибка при редактировании для Электроэнергии с 4 цифрами вместо 5")
    void shouldRejectUpdateWithWrongLengthForElectricity() throws Exception {
        Long elAccountId = createAccount("ELECTRICITY", "901-" + System.nanoTime());
        Long elReadingId = createReadingAndGetId(elAccountId, "28319", "2025-01-15");

        var request = new MeterReadingCreateRequest("1234", LocalDate.of(2025, 1, 20));

        mockMvc.perform(put("/accounts/{accId}/readings/{rId}", elAccountId, elReadingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("ровно 5 цифр")));
    }

    @Test
    @DisplayName("PUT — 404 при несуществующем показании")
    void shouldReturn404ForNonExistentReading() throws Exception {
        var request = new MeterReadingCreateRequest("0111", LocalDate.of(2025, 1, 20));

        mockMvc.perform(put("/accounts/{accId}/readings/{rId}", gasAccountId, 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT — ошибка при редактировании чужого показания")
    void shouldRejectUpdateOfReadingFromAnotherAccount() throws Exception {
        Long otherAccountId = createAccount("WATER", "902-" + System.nanoTime());

        var request = new MeterReadingCreateRequest("0111", LocalDate.of(2025, 1, 20));

        mockMvc.perform(put("/accounts/{accId}/readings/{rId}", otherAccountId, readingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("не принадлежит")));
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE — успешное удаление показания")
    void shouldDeleteReading() throws Exception {
        mockMvc.perform(delete("/accounts/{accId}/readings/{rId}", gasAccountId, readingId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/accounts/{id}/readings", gasAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("DELETE — 404 при несуществующем показании")
    void shouldReturn404WhenDeletingNonExistentReading() throws Exception {
        mockMvc.perform(delete("/accounts/{accId}/readings/{rId}", gasAccountId, 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE — ошибка при удалении чужого показания")
    void shouldRejectDeleteOfReadingFromAnotherAccount() throws Exception {
        Long otherAccountId = createAccount("GAS", "903-" + System.nanoTime());

        mockMvc.perform(delete("/accounts/{accId}/readings/{rId}", otherAccountId, readingId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("не принадлежит")));
    }

    @Test
    @DisplayName("DELETE — повторное удаление того же показания возвращает 404")
    void shouldReturn404OnDoubleDelete() throws Exception {
        mockMvc.perform(delete("/accounts/{accId}/readings/{rId}", gasAccountId, readingId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/accounts/{accId}/readings/{rId}", gasAccountId, readingId))
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

    private Long createReadingAndGetId(Long accId, String value, String date) throws Exception {
        var request = new MeterReadingCreateRequest(value, LocalDate.parse(date));
        String response = mockMvc.perform(post("/accounts/{id}/readings", accId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
