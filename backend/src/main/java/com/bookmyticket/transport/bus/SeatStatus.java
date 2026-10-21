package com.bookmyticket.transport.bus;

/**
 * Seat inventory states. HELD means a payment is pending for this seat and the
 * hold will expire automatically; only a verified payment moves it to BOOKED.
 */
public enum SeatStatus {
    AVAILABLE,
    HELD,
    BOOKED,
    BLOCKED
}
