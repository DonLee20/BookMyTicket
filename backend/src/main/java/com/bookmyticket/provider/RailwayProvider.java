package com.bookmyticket.provider;

import com.bookmyticket.transport.train.TrainService;
import com.bookmyticket.transport.train.TravelClass;

import java.time.LocalDate;

/**
 * Railway provider boundary. Indian Railways reservations require authorized
 * reservation channels, so this adapter never claims a real ticket: it either
 * returns sample availability (DEMO) or prepares an official booking handoff.
 */
public interface RailwayProvider extends TravelProvider {

    record Handoff(
            String reference,
            String url,
            String message,
            boolean ticketIssued
    ) {
    }

    String officialHandoffUrl();

    Handoff createHandoff(TrainService service, LocalDate journeyDate, TravelClass travelClass, int passengers);
}
