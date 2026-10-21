import { Link } from "react-router-dom";
import { Armchair, MapPin, Sparkles, Star } from "lucide-react";
import type { BusTrip } from "../lib/types";
import { DemoBadge } from "./Badges";

export function TripCard({ trip, premium = false }: { trip: BusTrip; premium?: boolean }) {
  const lowSeats = trip.availableSeats <= 6;
  return (
    <article className="card trip-card">
      <div>
        <div className="trip-card__operator">
          <span className="avatar" aria-hidden="true">
            {trip.operatorCode.slice(0, 2)}
          </span>
          <div>
            <div className="row" style={{ gap: "0.45rem" }}>
              <strong>{trip.operatorName}</strong>
              {trip.premium && (
                <span className="badge badge--dark">
                  <Sparkles size={12} aria-hidden="true" /> Premium
                </span>
              )}
              <DemoBadge mode={trip.providerMode} />
            </div>
            <div className="tiny muted">
              {trip.busTypeLabel} · {trip.cancellationPolicy.split(",")[0]}
            </div>
          </div>
        </div>
        <div className="times">
          <div>
            <div className="times__h">{trip.departTime}</div>
            <div className="times__meta">
              <MapPin size={12} aria-hidden="true" /> {trip.departureCity}
            </div>
          </div>
          <div className="times__line" aria-hidden="true" />
          <div>
            <div className="times__h">{trip.arriveTime}</div>
            <div className="times__meta">
              <MapPin size={12} aria-hidden="true" /> {trip.arrivalCity}
              {trip.arriveDayOffset > 0 ? ` (+${trip.arriveDayOffset}d)` : ""}
            </div>
          </div>
          <div className="tiny muted">{trip.durationLabel}</div>
        </div>
        <div className="amenity-list mt-2">
          {trip.amenities.slice(0, 4).map((amenity) => (
            <span key={amenity.code} className="badge">
              <Star size={11} aria-hidden="true" /> {amenity.label}
            </span>
          ))}
        </div>
      </div>

      <div className="small muted">
        <div className="strong" style={{ color: "var(--ink)" }}>
          Boarding
        </div>
        <div>{trip.boardingPoints.slice(0, 2).join(", ")}</div>
        <div className="strong mt-1" style={{ color: "var(--ink)" }}>
          Dropping
        </div>
        <div>{trip.droppingPoints.slice(0, 2).join(", ")}</div>
        <div className="mt-1">
          <Armchair size={13} aria-hidden="true" />{" "}
          <span className={lowSeats ? "badge badge--warn" : ""}>
            {trip.availableSeats} of {trip.totalSeats} seats open
          </span>
        </div>
      </div>

      <div className="fare">
        <div className="tiny muted">from</div>
        <div className="fare__amount">{trip.fareDisplay}</div>
        <div className="tiny muted mb-2">
          per traveller{trip.providerMode === "LIVE" ? "" : " · demo fare"}
        </div>
        {trip.seatSelectionSupported ? (
          <Link to={`/buses/${trip.id}/seats?date=${trip.journeyDate}`} className="btn btn--primary btn--sm btn--block">
            Select seats
          </Link>
        ) : (
          <span className="badge badge--warn">Seat map unavailable</span>
        )}
        <div className="tiny muted mt-1 text-center">
          {premium ? "Premium coach" : trip.providerMode === "LIVE" ? "Live inventory" : "Demo inventory"}
        </div>
      </div>
    </article>
  );
}
