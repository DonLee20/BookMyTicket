package com.bookmyticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end customer journey over HTTP: register, search, hold seats, complete
 * the simulated payment, then cancel. Everything runs against the throwaway
 * MySQL created by scripts/test.sh.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerSearchBookPayAndCancelBusFlow() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String email = "traveller-" + UUID.randomUUID() + "@example.test";
        register(session, email);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.roles[0]").value("ROLE_USER"));

        LocalDate date = LocalDate.now().plusDays(1);
        JsonNode search = json(mockMvc.perform(post("/api/buses/search")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "from", "Chennai",
                                "to", "Coimbatore",
                                "date", date.toString(),
                                "passengers", 1))))
                .andExpect(status().isOk())
                .andReturn());

        JsonNode trip = search.get("trips").get(0);
        long tripId = trip.get("id").asLong();
        String boardingPoint = trip.get("boardingPoints").get(0).asText();
        String droppingPoint = trip.get("droppingPoints").get(0).asText();

        JsonNode seatMap = json(mockMvc.perform(get("/api/buses/trips/" + tripId + "/seats").session(session))
                .andExpect(status().isOk())
                .andReturn());
        JsonNode seat = firstAvailableSeat(seatMap.get("seatMap"));
        long seatId = seat.get("id").asLong();
        long priceMinor = seat.get("priceMinor").asLong();

        MvcResult created = mockMvc.perform(post("/api/bookings/bus")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tripId", tripId,
                                "seatIds", List.of(seatId),
                                "boardingPoint", boardingPoint,
                                "droppingPoint", droppingPoint,
                                "contactName", "Test Traveller",
                                "contactEmail", email,
                                "contactPhone", "+91 90000 00000",
                                "passengers", List.of(Map.of(
                                        "fullName", "Test Traveller",
                                        "age", 30,
                                        "gender", "MALE",
                                        "passengerType", "ADULT")),
                                "expectedTotalMinor", priceMinor,
                                "idempotencyKey", "it-bus-" + UUID.randomUUID()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"))
                .andExpect(jsonPath("$.payment.status").value("PENDING"))
                .andReturn();

        JsonNode booking = json(created);
        String bookingId = booking.get("id").asText();
        String paymentId = booking.get("payment").get("id").asText();
        assertThat(booking.get("totalAmountMinor").asLong()).isEqualTo(priceMinor);

        mockMvc.perform(post("/api/payments/" + paymentId + "/demo-complete")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"outcome\":\"SUCCESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        mockMvc.perform(get("/api/bookings/" + bookingId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        mockMvc.perform(get("/api/bookings").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(bookingId));

        mockMvc.perform(post("/api/bookings/" + bookingId + "/cancel")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Integration test cancellation\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.paymentStatus").value("REFUNDED"))
                .andExpect(jsonPath("$.refundAmountMinor").isNumber());
    }

    @Test
    void trainRequestReturnsOfficialIrctcHandoff() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String email = "rail-" + UUID.randomUUID() + "@example.test";
        register(session, email);

        LocalDate date = LocalDate.now().plusDays(1);
        JsonNode search = json(mockMvc.perform(post("/api/trains/search")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "from", "MAS",
                                "to", "CBE",
                                "date", date.toString(),
                                "passengers", 1))))
                .andExpect(status().isOk())
                .andReturn());

        JsonNode train = search.get("trains").get(0);
        String travelClass = train.get("classes").get(0).get("code").asText();

        mockMvc.perform(post("/api/bookings/train")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("serviceId", train.get("id").asLong()),
                                Map.entry("travelClass", travelClass),
                                Map.entry("quota", "GENERAL"),
                                Map.entry("journeyDate", date.toString()),
                                Map.entry("fromStation", train.get("fromName").asText()),
                                Map.entry("toStation", train.get("toName").asText()),
                                Map.entry("contactName", "Test Traveller"),
                                Map.entry("contactEmail", email),
                                Map.entry("contactPhone", "+91 90000 00000"),
                                Map.entry("passengers", List.of(Map.of(
                                        "fullName", "Test Traveller",
                                        "age", 30,
                                        "gender", "MALE",
                                        "passengerType", "ADULT"))),
                                Map.entry("idempotencyKey", "it-train-" + UUID.randomUUID())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentStatus").value("NOT_REQUIRED"))
                .andExpect(jsonPath("$.nextAction").value("EXTERNAL_HANDOFF"))
                .andExpect(jsonPath("$.officialHandoffUrl").value(org.hamcrest.Matchers.startsWith("https://www.irctc.co.in")));
    }

    @Test
    void duplicateRegistrationIsRejected() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String email = "duplicate-" + UUID.randomUUID() + "@example.test";
        register(session, email);
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .session(new MockHttpSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_IN_USE"));
    }

    @Test
    void invalidCredentialsAreRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.test\",\"password\":\"not-the-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    private void register(MockHttpSession session, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value(email));
    }

    private String registrationBody(String email) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "name", "Test Traveller",
                "email", email,
                "password", "Integration#2026",
                "phone", "+91 90000 00000"));
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode firstAvailableSeat(JsonNode seatMap) {
        for (JsonNode deck : seatMap.get("decks")) {
            for (JsonNode row : deck.get("rows")) {
                for (JsonNode seat : row.get("seats")) {
                    if ("AVAILABLE".equals(seat.get("status").asText())) {
                        return seat;
                    }
                }
            }
        }
        throw new AssertionError("Seeded demo inventory has no available seats");
    }
}
