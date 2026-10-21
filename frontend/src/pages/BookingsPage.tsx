import { Link, useSearchParams } from "react-router-dom";
import { BedDouble, BusFront, CalendarSearch, Plane, Sparkles, TrainFront } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { EmptyState, ErrorState, LoadingBlock } from "../components/Feedback";
import { StatusBadge } from "../components/Badges";
import { useAsync } from "../hooks/useAsync";
import { api } from "../lib/api";
import { formatDateShort, formatDateTime, humanise } from "../lib/format";
import type { Booking, Page } from "../lib/types";

const categoryIcon: Record<string, typeof BusFront> = {
  BUS: BusFront,
  PREMIUM_BUS: Sparkles,
  TRAIN: TrainFront,
  FLIGHT: Plane,
  HOLIDAY: BedDouble,
  MTC: CalendarSearch,
};

export function BookingsPage() {
  const [params, setParams] = useSearchParams();
  const status = params.get("status") ?? "";
  const category = params.get("category") ?? "";
  const page = Number(params.get("page") ?? 0);

  const query = new URLSearchParams();
  if (status) query.set("status", status);
  if (category) query.set("category", category);
  query.set("page", String(page));
  query.set("size", "8");

  const { data, loading, error, reload } = useAsync<Page<Booking>>(
    (signal) => api.get(`/api/bookings?${query.toString()}`, signal),
    [status, category, page],
  );

  const update = (key: string, value: string) => {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    next.delete("page");
    setParams(next, { replace: true });
  };

  return (
    <div className="container">
      <PageHead
        eyebrow="Your account"
        title="My bookings"
        lede="Every bus, premium coach, rail request, flight request and holiday package in one place — with the exact status of each step."
        crumbs={[{ to: "/", label: "Home" }, { label: "My Bookings" }]}
        actions={
          <Link to="/" className="btn btn--lime">
            Plan a new journey
          </Link>
        }
      />

      <div className="panel mb-3">
        <div className="row row--wrap">
          <label className="field" style={{ minWidth: 220 }}>
            <span className="field__label">Status</span>
            <select className="select" value={status} onChange={(event) => update("status", event.target.value)}>
              <option value="">All statuses</option>
              {["PENDING", "PAYMENT_PENDING", "CONFIRMED", "CANCELLED", "REFUNDED", "FAILED", "EXPIRED"].map((value) => (
                <option key={value} value={value}>
                  {humanise(value)}
                </option>
              ))}
            </select>
          </label>
          <label className="field" style={{ minWidth: 220 }}>
            <span className="field__label">Category</span>
            <select className="select" value={category} onChange={(event) => update("category", event.target.value)}>
              <option value="">All categories</option>
              {["BUS", "PREMIUM_BUS", "TRAIN", "FLIGHT", "HOLIDAY"].map((value) => (
                <option key={value} value={value}>
                  {humanise(value)}
                </option>
              ))}
            </select>
          </label>
          {data && (
            <div className="small muted" style={{ marginLeft: "auto" }}>
              {data.total} booking{data.total === 1 ? "" : "s"}
            </div>
          )}
        </div>
      </div>

      {loading && <LoadingBlock label="Loading your bookings" />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && data && data.items.length === 0 && (
        <EmptyState title="No bookings yet" icon={<BusFront size={30} aria-hidden="true" />}>
          <p>Start with a bus seat map demo, a rail planning request or a holiday itinerary.</p>
          <div className="row mt-2" style={{ justifyContent: "center" }}>
            <Link to="/buses" className="btn btn--primary btn--sm">
              Search buses
            </Link>
            <Link to="/holidays" className="btn btn--ghost btn--sm">
              Browse holidays
            </Link>
          </div>
        </EmptyState>
      )}

      {!loading &&
        data?.items.map((booking) => {
          const Icon = categoryIcon[booking.category] ?? BusFront;
          return (
            <Link key={booking.id} to={`/bookings/${booking.id}`} className="card card--pad card--hover mt-2" style={{ display: "block" }}>
              <div className="spread">
                <div className="row" style={{ gap: "0.8rem" }}>
                  <span className="avatar" aria-hidden="true">
                    <Icon size={19} />
                  </span>
                  <div>
                    <div className="row row--wrap" style={{ gap: "0.45rem" }}>
                      <strong>{bookingTitle(booking)}</strong>
                      <StatusBadge status={booking.status} label={booking.statusLabel} />
                      {booking.paymentStatus !== "NOT_REQUIRED" && (
                        <StatusBadge status={booking.paymentStatus} />
                      )}
                    </div>
                    <div className="small muted">
                      {booking.id} · {booking.categoryLabel} · created {formatDateTime(booking.createdAt)}
                    </div>
                    <div className="small muted">{bookingSubtitle(booking)}</div>
                  </div>
                </div>
                <div className="fare">
                  <div className="fare__amount">{booking.totalDisplay}</div>
                  <div className="tiny muted">{booking.travellerCount} traveller(s)</div>
                  {booking.nextAction && (
                    <span className="badge badge--warn mt-1">{humanise(booking.nextAction)}</span>
                  )}
                </div>
              </div>
            </Link>
          );
        })}

      {data && (data.page > 0 || data.hasNext) && (
        <div className="row mt-3" style={{ justifyContent: "center" }}>
          <button
            type="button"
            className="btn btn--ghost btn--sm"
            disabled={data.page === 0}
            onClick={() => update("page", String(data.page - 1))}
          >
            Previous
          </button>
          <span className="small muted">Page {data.page + 1}</span>
          <button
            type="button"
            className="btn btn--ghost btn--sm"
            disabled={!data.hasNext}
            onClick={() => update("page", String(data.page + 1))}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}

export function bookingTitle(booking: Booking): string {
  if (booking.details.bus) {
    return `${booking.details.bus.departureCity} → ${booking.details.bus.arrivalCity}`;
  }
  if (booking.details.train) {
    return `${booking.details.train.trainNumber} ${booking.details.train.trainName}`;
  }
  if (booking.details.flight) {
    return `${booking.details.flight.airlineName} ${booking.details.flight.flightNumber}`;
  }
  if (booking.details.holiday) {
    return booking.details.holiday.packageTitle;
  }
  return booking.categoryLabel;
}

export function bookingSubtitle(booking: Booking): string {
  if (booking.details.bus) {
    return `${booking.details.bus.operatorName} · ${booking.details.bus.busType.replaceAll("_", " ").toLowerCase()} · seats ${
      booking.details.bus.seatNumbers.join(", ")
    } · ${formatDateShort(booking.details.bus.travelDate)}`;
  }
  if (booking.details.train) {
    return `${booking.details.train.fromStation} → ${booking.details.train.toStation} · ${
      booking.details.train.travelClass.replaceAll("_", " ")
    } · ${formatDateShort(booking.details.train.journeyDate)}`;
  }
  if (booking.details.flight) {
    return `${booking.details.flight.originIata} → ${booking.details.flight.destinationIata} · ${
      booking.details.flight.cabin
    } · ${formatDateShort(booking.details.flight.journeyDate)}`;
  }
  if (booking.details.holiday) {
    return `${booking.details.holiday.destinationName} · ${formatDateShort(booking.details.holiday.startDate)} to ${formatDateShort(
      booking.details.holiday.endDate,
    )} · ${booking.details.holiday.travellers} traveller(s)`;
  }
  return "";
}
