package com.bookmyticket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Public catalog and search endpoints. Runs against the throwaway MySQL
 * prepared by scripts/test.sh with the full Flyway migration set applied.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicCatalogEndpointsReturnData() throws Exception {
        mockMvc.perform(get("/api/holidays/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featuredPackages").isArray())
                .andExpect(jsonPath("$.featuredDestinations").isArray());

        mockMvc.perform(get("/api/holidays/packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packages").isNotEmpty())
                .andExpect(jsonPath("$.destinations").isNotEmpty());

        mockMvc.perform(get("/api/buses/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cities").isNotEmpty());

        mockMvc.perform(get("/api/buses/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/trains/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/flights/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/mtc/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/premium/highlights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comfortCategories").isNotEmpty())
                .andExpect(jsonPath("$.providerMode").value("DEMO"));
    }

    @Test
    void busSearchReturnsSeededDemoTrips() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        mockMvc.perform(post("/api/buses/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from":"Chennai","to":"Coimbatore","date":"%s","passengers":1}
                                """.formatted(tomorrow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerMode").value("DEMO"))
                .andExpect(jsonPath("$.trips").isNotEmpty())
                .andExpect(jsonPath("$.trips[0].seatSelectionSupported").value(true))
                .andExpect(jsonPath("$.liveDataConnected").value(false))
                .andExpect(jsonPath("$.liveProviderConfigured").value(false))
                .andExpect(jsonPath("$.diagnostics.status").value("OK"))
                .andExpect(jsonPath("$.diagnostics.routeCovered").value(true))
                .andExpect(jsonPath("$.diagnostics.tripsOnDate").isNumber());
    }

    @Test
    void unsupportedBusRouteGetsGeneratedDemoTrips() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        mockMvc.perform(post("/api/buses/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from":"Hyderabad","to":"Digha","date":"%s","passengers":1}
                                """.formatted(tomorrow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isNotEmpty())
                .andExpect(jsonPath("$.trips[0].source").value("DEMO"))
                .andExpect(jsonPath("$.trips[0].seatSelectionSupported").value(true))
                .andExpect(jsonPath("$.diagnostics.status").value("OK"))
                .andExpect(jsonPath("$.diagnostics.originCovered").value(true))
                .andExpect(jsonPath("$.diagnostics.destinationCovered").value(true))
                .andExpect(jsonPath("$.diagnostics.routeCovered").value(true));
    }

    @Test
    void unknownBusCitiesAreSupportedWithGeneratedDemoTrips() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        mockMvc.perform(post("/api/buses/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from":"Atlantis","to":"Digha","date":"%s","passengers":1}
                                """.formatted(tomorrow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isNotEmpty())
                .andExpect(jsonPath("$.diagnostics.status").value("OK"))
                .andExpect(jsonPath("$.diagnostics.originCovered").value(true))
                .andExpect(jsonPath("$.diagnostics.destinationCovered").value(true));

        mockMvc.perform(post("/api/buses/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from":"Hyderabad","to":"Atlantis","date":"%s","passengers":1}
                                """.formatted(tomorrow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isNotEmpty())
                .andExpect(jsonPath("$.diagnostics.status").value("OK"))
                .andExpect(jsonPath("$.diagnostics.originCovered").value(true))
                .andExpect(jsonPath("$.diagnostics.destinationCovered").value(true));
    }

    @Test
    void busSearchGeneratesInventoryOutsideStartupWindow() throws Exception {
        LocalDate outsideWindow = LocalDate.now().plusDays(45);
        mockMvc.perform(post("/api/buses/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from":"Chennai","to":"Coimbatore","date":"%s","passengers":1}
                                """.formatted(outsideWindow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isNotEmpty())
                .andExpect(jsonPath("$.diagnostics.status").value("OK"))
                .andExpect(jsonPath("$.diagnostics.routeCovered").value(true))
                .andExpect(jsonPath("$.diagnostics.dateInInventoryWindow").value(true))
                .andExpect(jsonPath("$.diagnostics.inventoryFrom").isNotEmpty())
                .andExpect(jsonPath("$.diagnostics.inventoryTo").value(outsideWindow.toString()));
    }

    @Test
    void trainSearchGeneratesSampleServicesForAnyRoute() throws Exception {
        LocalDate date = LocalDate.now().plusDays(2);
        mockMvc.perform(post("/api/trains/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from":"Atlantis","to":"El Dorado","date":"%s","passengers":1}
                                """.formatted(date)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerMode").value("DEMO"))
                .andExpect(jsonPath("$.trains").isNotEmpty())
                .andExpect(jsonPath("$.trains[0].runsOnDate").value(true))
                .andExpect(jsonPath("$.trains[0].classes").isNotEmpty())
                .andExpect(jsonPath("$.trains[0].routeStops").isNotEmpty());
    }

    @Test
    void busSearchReportsFilteredOutSeparatelyFromNoTrips() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        mockMvc.perform(post("/api/buses/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from":"Chennai","to":"Coimbatore","date":"%s","passengers":1,"maxFareMinor":1}
                                """.formatted(tomorrow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isEmpty())
                .andExpect(jsonPath("$.diagnostics.status").value("FILTERED_OUT"))
                .andExpect(jsonPath("$.diagnostics.routeCovered").value(true))
                .andExpect(jsonPath("$.diagnostics.tripsOnDate", org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.diagnostics.tripsAfterFilters").value(0));
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void unknownApiPathReturnsJsonNotFound() throws Exception {
        mockMvc.perform(get("/api/holidays/definitely-not-a-route"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void unsafeRequestWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/api/buses/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from":"Chennai","to":"Coimbatore","date":"%s","passengers":1}
                                """.formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isForbidden());
    }
}
