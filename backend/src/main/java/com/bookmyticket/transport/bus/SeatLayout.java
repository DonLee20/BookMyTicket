package com.bookmyticket.transport.bus;

public enum SeatLayout {
    SEATER_2X2(2, 2, SeatType.SEATER, false),
    SEMI_SLEEPER_2X2(2, 2, SeatType.SEATER, false),
    SLEEPER_2X1(2, 1, SeatType.SLEEPER, true);

    private final int seatsPerRowLeft;
    private final int seatsPerRowRight;
    private final SeatType seatType;
    private final boolean doubleDeck;

    SeatLayout(int seatsPerRowLeft, int seatsPerRowRight, SeatType seatType, boolean doubleDeck) {
        this.seatsPerRowLeft = seatsPerRowLeft;
        this.seatsPerRowRight = seatsPerRowRight;
        this.seatType = seatType;
        this.doubleDeck = doubleDeck;
    }

    public int seatsPerRowLeft() {
        return seatsPerRowLeft;
    }

    public int seatsPerRowRight() {
        return seatsPerRowRight;
    }

    public int seatsPerRow() {
        return seatsPerRowLeft + seatsPerRowRight;
    }

    public SeatType seatType() {
        return seatType;
    }

    public boolean hasUpperDeck() {
        return doubleDeck;
    }

    public int rowsPerDeck() {
        return switch (this) {
            case SLEEPER_2X1 -> 7;
            default -> 10;
        };
    }
}
