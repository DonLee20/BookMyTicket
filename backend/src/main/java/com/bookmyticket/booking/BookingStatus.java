package com.bookmyticket.booking;

import java.util.EnumSet;
import java.util.Set;

/**
 * Booking lifecycle. Category-specific rules restrict which transitions and
 * states are legal; for example only paid categories can become CONFIRMED, and
 * TICKETED is reserved for a real provider ticket (not reachable in demo mode).
 */
public enum BookingStatus {
    DRAFT,
    PENDING,
    PAYMENT_PENDING,
    CONFIRMED,
    TICKETED,
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    REFUNDED,
    EXPIRED;

    private static final Set<BookingStatus> TERMINAL = EnumSet.of(FAILED, CANCELLED, REFUNDED, EXPIRED, TICKETED);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }

    public boolean isCancellable() {
        return this == PENDING || this == PAYMENT_PENDING || this == CONFIRMED || this == TICKETED;
    }

    public boolean isPaidHold() {
        return this == PAYMENT_PENDING;
    }

    public static boolean canTransition(BookingStatus from, BookingStatus to) {
        return switch (from) {
            case DRAFT -> to == PENDING || to == PAYMENT_PENDING || to == CANCELLED;
            case PENDING -> to == CONFIRMED || to == FAILED || to == CANCELLED || to == EXPIRED;
            case PAYMENT_PENDING -> to == CONFIRMED || to == FAILED || to == CANCELLED || to == EXPIRED;
            case CONFIRMED -> to == TICKETED || to == CANCELLED || to == REFUND_PENDING;
            case TICKETED -> to == CANCELLED || to == REFUND_PENDING;
            case REFUND_PENDING -> to == REFUNDED || to == CONFIRMED;
            default -> false;
        };
    }
}
