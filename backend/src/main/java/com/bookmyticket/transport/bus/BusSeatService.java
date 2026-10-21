package com.bookmyticket.transport.bus;

import com.bookmyticket.common.ApiException;
import com.bookmyticket.common.MoneyUtils;
import com.bookmyticket.provider.BusProvider;
import com.bookmyticket.provider.ProviderRegistry;
import com.bookmyticket.transport.bus.dto.BusDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class BusSeatService {

    private final BusTripRepository tripRepository;
    private final BusSeatRepository seatRepository;
    private final BusSearchService searchService;
    private final ProviderRegistry providerRegistry;

    public BusSeatService(BusTripRepository tripRepository, BusSeatRepository seatRepository,
                          BusSearchService searchService, ProviderRegistry providerRegistry) {
        this.tripRepository = tripRepository;
        this.seatRepository = seatRepository;
        this.searchService = searchService;
        this.providerRegistry = providerRegistry;
    }

    @Transactional(readOnly = true)
    public BusDtos.SeatMapResponse seatMap(Long tripId) {
        providerRegistry.ensureAvailable(BusProvider.class);
        BusTrip trip = tripRepository.findDetailedById(tripId)
                .orElseThrow(() -> ApiException.notFound("TRIP_NOT_FOUND", "That bus service could not be found."));
        List<BusSeat> seats = seatRepository.findByTripIdOrderByDeckAscRowNoAscColNoAsc(tripId);
        if (seats.isEmpty()) {
            throw ApiException.notFound("SEAT_MAP_UNAVAILABLE",
                    "No seat map is published for this service yet.");
        }

        int available = 0;
        int total = 0;
        Map<SeatDeck, Map<Integer, List<BusSeat>>> byDeck = new LinkedHashMap<>();
        for (BusSeat seat : seats) {
            total++;
            if (seat.getStatus() == SeatStatus.AVAILABLE) {
                available++;
            }
            byDeck.computeIfAbsent(seat.getDeck(), d -> new TreeMap<>())
                    .computeIfAbsent(seat.getRowNo(), r -> new ArrayList<>())
                    .add(seat);
        }

        List<BusDtos.DeckView> decks = new ArrayList<>();
        for (Map.Entry<SeatDeck, Map<Integer, List<BusSeat>>> deckEntry : byDeck.entrySet()) {
            List<BusDtos.SeatRow> rows = new ArrayList<>();
            for (Map.Entry<Integer, List<BusSeat>> rowEntry : deckEntry.getValue().entrySet()) {
                List<BusDtos.SeatView> rowSeats = rowEntry.getValue().stream()
                        .map(seat -> toSeatView(seat, trip))
                        .toList();
                rows.add(new BusDtos.SeatRow(rowEntry.getKey(), rowSeats));
            }
            decks.add(new BusDtos.DeckView(deckEntry.getKey().name(), deckLabel(deckEntry.getKey()), rows));
        }

        BusDtos.SeatMap seatMap = new BusDtos.SeatMap(
                trip.getId(),
                trip.getLayout().name(),
                layoutLabel(trip.getLayout()),
                trip.getFareMinor(),
                MoneyUtils.format(trip.getFareMinor(), trip.getCurrency()),
                trip.getCurrency(),
                available,
                total,
                decks,
                providerRegistry.effectiveModeFor(BusProvider.class).name());

        BusDtos.TripResult tripResult = searchService.toResult(trip, available);
        return new BusDtos.SeatMapResponse(tripResult, seatMap);
    }

    private BusDtos.SeatView toSeatView(BusSeat seat, BusTrip trip) {
        boolean window = seat.getColNo() == 1 || seat.getColNo() == trip.getLayout().seatsPerRow();
        return new BusDtos.SeatView(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getSeatType().name(),
                seat.getDeck().name(),
                seat.getRowNo(),
                seat.getColNo(),
                seat.getStatus().name(),
                seat.getPriceMinor(),
                MoneyUtils.format(seat.getPriceMinor(), trip.getCurrency()),
                window);
    }

    private String deckLabel(SeatDeck deck) {
        return switch (deck) {
            case LOWER -> "Lower deck";
            case UPPER -> "Upper deck";
        };
    }

    private String layoutLabel(SeatLayout layout) {
        return switch (layout) {
            case SEATER_2X2 -> "2 x 2 seater";
            case SEMI_SLEEPER_2X2 -> "2 x 2 semi-sleeper";
            case SLEEPER_2X1 -> "2 x 1 sleeper (lower + upper deck)";
        };
    }
}
