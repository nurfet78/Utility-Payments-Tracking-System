package org.nurfet.paymentstrackingsystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /services — возвращает все 8 услуг")
    void shouldReturnAllServices() throws Exception {
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[?(@.code == 'GAS')].hasMeter").value(true))
                .andExpect(jsonPath("$[?(@.code == 'GAS')].meterDigits").value(4))
                .andExpect(jsonPath("$[?(@.code == 'ELECTRICITY')].meterDigits").value(5))
                .andExpect(jsonPath("$[?(@.code == 'INTERCOM')].hasMeter").value(false))
                .andExpect(jsonPath("$[?(@.code == 'INTERCOM')].meterDigits").value(everyItem(nullValue())));
    }
}
