package com.bookmyticket.provider;

import com.bookmyticket.config.AppProperties;
import com.bookmyticket.transport.bus.BusSeat;
import com.bookmyticket.transport.bus.BusTrip;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Demonstration bus inventory adapter. Reads the local demo dataset and is the
 * server-side fare authority. It never creates a real operator reservation.
 */
@Component
public class DemoBusProvider implements BusProvider {

    private final AppProperties properties;

    public DemoBusProvider(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public String id() {
        return "demo-bus-inventory";
    }

    @Override
    public String displayName() {
        return "Demo bus inventory (local sample dataset)";
    }

    @Override
    public String category() {
        return "BUS";
    }

    @Override
    public ProviderMode mode() {
        return ProviderMode.from(properties.getProviders().getBus().getMode(), ProviderMode.DEMO);
    }

    @Override
    public boolean bookingSupported() {
        return true;
    }

    @Override
    public List<String> capabilities() {
        return List.of("SEARCH", "SEAT_MAP", "PRICE_VALIDATION", "DEMO_RESERVATION");
    }

    @Override
    public FareQuote quote(BusTrip trip, List<BusSeat> seats, int passengers) {
        long total = seats.stream().mapToLong(BusSeat::getPriceMinor).sum();
        long perSeat = seats.isEmpty() ? trip.getFareMinor() : total / seats.size();
        List<String> numbers = seats.stream().map(BusSeat::getSeatNumber).toList();
        return new FareQuote(total, trip.getCurrency(), numbers, perSeat);
    }

    @Override
    public boolean supportsSeatSelection() {
        return true;
    }

    @Override
    public String statusMessage() {
        return "Sample inventory: no real operator reservation is created; seats are held and confirmed inside this "
                + "platform only for demonstration.";
    }
}
