package com.bookmyticket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A LIVE bus configuration without a live adapter must fail loudly instead of
 * returning demo inventory.
 */
@SpringBootTest(properties = {
        "app.providers.bus.mode=LIVE",
        "app.demo.auto-seed-inventory=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BusProviderUnavailableTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void liveBusModeWithoutAdapterReturns503AndConfigurationGuidance() throws Exception {
        mockMvc.perform(post("/api/buses/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from":"Hyderabad","to":"Digha","date":"%s","passengers":1}
                                """.formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("BUSPROVIDER_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value(containsString("Live bus data is not connected")))
                .andExpect(jsonPath("$.message").value(containsString("BUS_LIVE_")));
    }
}
