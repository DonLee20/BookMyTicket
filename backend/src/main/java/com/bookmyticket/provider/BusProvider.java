package com.bookmyticket.provider;

import com.bookmyticket.transport.bus.BusSeat;
import com.bookmyticket.transport.bus.BusTrip;

import java.util.List;

/**
 * Bus inventory provider boundary. The demo implementation reads the local
 * demonstration inventory; a live adapter would call an authorized bus
 * inventory API here.
 */
public interface BusProvider extends TravelProvider {

    record FareQuote(long totalMinor, String currency, List<String> seatNumbers, long farePerSeatMinor) {
    }

    /** Server-side fare authority: the client never decides the payable amount. */
    FareQuote quote(BusTrip trip, List<BusSeat> seats, int passengers);

    boolean supportsSeatSelection();
}
