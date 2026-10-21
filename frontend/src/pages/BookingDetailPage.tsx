import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  AlertTriangle,
  ArrowLeft,
  BadgeCheck,
  BedDouble,
  BusFront,
  CreditCard,
  ExternalLink,
  Plane,
  ShieldAlert,
  Sparkles,
  Timer,
  TrainFront,
  XCircle,
} from "lucide-react";
import { PageHead } from "../components/PageHead";
import { ErrorState, LoadingBlock } from "../components/Feedback";
import { StatusBadge } from "../components/Badges";
import { useAsync } from "../hooks/useAsync";
import { useToast } from "../context/ToastContext";
import { api, ApiError } from "../lib/api";
import { formatDateLong, formatDateTime, formatMoney, humanise } from "../lib/format";
import type { Booking, DemoCompletionResult } from "../lib/types";
import { bookingTitle } from "./BookingsPage";

export function BookingDetailPage() {
  const { bookingId = "" } = useParams();
  const { notify } = useToast();
  const [cancelling, setCancelling] = useState(false);
  const [paying, setPaying] = useState<null | "SUCCESS" | "FAILURE">(null);
  const [cancelReason, setCancelReason] = useState("");
  const [secondsLeft, setSecondsLeft] = useState<number | null>(null);

  const { data: booking, loading, error, reload } = useAsync<Booking>(
    (signal) => api.get(`/api/bookings/${bookingId}`, signal),
    [bookingId],
  );

  useEffect(() => {
    if (booking?.holdSecondsRemaining == null) {
      setSecondsLeft(null);
      return;
    }
    setSecondsLeft(booking.holdSecondsRemaining);
    const timer = window.setInterval(() => {
      setSecondsLeft((current) => (current == null ? null : Math.max(0, current - 1)));
    }, 1000);
    return () => window.clearInterval(timer);
  }, [booking?.holdSecondsRemaining, booking?.updatedAt]);

  const holdLabel = useMemo(() => {
    if (secondsLeft == null) return null;
    const minutes = Math.floor(secondsLeft / 60);
    const seconds = secondsLeft % 60;
    return `${minutes}:${String(seconds).padStart(2, "0")}`;
  }, [secondsLeft]);

  const completeDemoPayment = async (outcome: "SUCCESS" | "FAILURE") => {
    const paymentId = booking?.payment?.id;
    if (!paymentId) return;
    setPaying(outcome);
    try {
      const result = await api.post<DemoCompletionResult>(`/api/payments/${paymentId}/demo-complete`, { outcome });
      if (outcome === "SUCCESS") {
        notify(
          result.duplicate
            ? "This simulated payment was already completed."
            : "Simulated payment successful. Your booking is confirmed.",
          "ok",
        );
      } else {
        notify("Simulated failure recorded. The booking was not confirmed.", "error");
      }
      reload();
    } catch (err) {
      notify(err instanceof ApiError ? err.message : "We couldn’t complete the simulated payment.", "error");
    } finally {
      setPaying(null);
    }
  };

  const cancelBooking = async () => {
    if (!booking) return;
    try {
      await api.post<Booking>(`/api/bookings/${booking.id}/cancel`, { reason: cancelReason });
      notify("Booking cancelled. Any applicable demo refund has been recorded.", "ok");
      setCancelling(false);
      reload();
    } catch (err) {
      notify(err instanceof ApiError ? err.message : "We couldn’t cancel this booking.", "error");
    }
  };

  if (loading) {
    return (
      <div className="container section">
        <LoadingBlock label="Loading your booking" />
      </div>
    );
  }

  if (error || !booking) {
    return (
      <div className="container section">
        <ErrorState message={error ?? "That booking could not be found."} onRetry={reload} />
        <Link to="/bookings" className="btn btn--ghost mt-3">
          <ArrowLeft size={16} aria-hidden="true" /> All bookings
        </Link>
      </div>
    );
  }

  const Icon = categoryIcon(booking.category);
  const isDemoPaymentPending = booking.payment?.mode === "DEMO" && booking.payment.status === "PENDING";
  const canCancel = ["PENDING", "PAYMENT_PENDING", "CONFIRMED", "TICKETED"].includes(booking.status);

  return (
    <div className="container">
      <PageHead
        eyebrow={booking.id}
        title={bookingTitle(booking)}
        lede={booking.message}
        crumbs={[
          { to: "/", label: "Home" },
          { to: "/bookings", label: "My Bookings" },
          { label: booking.id },
        ]}
        actions={
          <>
            <StatusBadge status={booking.status} label={booking.statusLabel} />
            {booking.paymentStatus !== "NOT_REQUIRED" && <StatusBadge status={booking.paymentStatus} />}
          </>
        }
      />

      {booking.status === "EXPIRED" && (
        <div className="notice notice--danger mb-3">
          <XCircle size={18} aria-hidden="true" />
          <span>The hold on this booking expired before payment was completed, so the inventory was released.</span>
        </div>
      )}

      <div className="sidebar-layout mb-4">
        <div>
          <div className="card card--pad">
            <div className="row" style={{ gap: "0.8rem" }}>
              <span className="avatar" aria-hidden="true">
                <Icon size={20} />
              </span>
              <div>
                <strong>{booking.categoryLabel} booking</strong>
                <div className="small muted">
                  Created {formatDateTime(booking.createdAt)} · provider mode {booking.providerMode}
                </div>
              </div>
            </div>
            <div className="divider" />
            {booking.details.bus && <BusDetails booking={booking} />}
            {booking.details.train && <TrainDetails booking={booking} />}
            {booking.details.flight && <FlightDetails booking={booking} />}
            {booking.details.holiday && <HolidayDetails booking={booking} />}
          </div>

          <div className="card card--pad mt-3">
            <h2>Status timeline</h2>
            <div className="timeline mt-2">
              <TimelineStep
                title="Request created"
                body={`${booking.categoryLabel} request recorded at ${formatDateTime(booking.createdAt)}.`}
                state="done"
              />
              {booking.paymentStatus !== "NOT_REQUIRED" ? (
                <TimelineStep
                  title="Payment"
                  body={
                    booking.payment?.message ??
                    (booking.paymentStatus === "PAID" ? "Payment recorded." : "Awaiting payment.")
                  }
                  state={["PAID", "REFUNDED"].includes(booking.paymentStatus) ? "done" : "current"}
                />
              ) : (
                <TimelineStep title="Payment not required" body="This request is completed on an external provider channel." state="done" />
              )}
              <TimelineStep
                title={booking.statusLabel}
                body={booking.nextAction ? `Next action: ${humanise(booking.nextAction)}.` : "No further action needed."}
                state={booking.status === "CANCELLED" || booking.status === "FAILED" ? "pending" : "current"}
              />
            </div>
            {holdLabel && secondsLeft != null && (
              <div className={`notice ${secondsLeft < 120 ? "notice--danger" : "notice--warn"} mt-2`}>
                <Timer size={17} aria-hidden="true" />
                <span>
                  {secondsLeft > 0
                    ? `Seats are held for another ${holdLabel} minute(s). Confirm with the simulated payment to continue.`
                    : "The hold window has ended. The seats will be released shortly."}
                </span>
              </div>
            )}
          </div>

          {booking.passengers.length > 0 && (
            <div className="card card--pad mt-3">
              <h2>Travellers</h2>
              <div className="table-wrap mt-2">
                <table className="table">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Name</th>
                      <th>Age</th>
                      <th>Gender</th>
                      <th>Type</th>
                      <th>Seat</th>
                    </tr>
                  </thead>
                  <tbody>
                    {booking.passengers.map((passenger) => (
                      <tr key={passenger.seq}>
                        <td>{passenger.seq}</td>
                        <td>{passenger.fullName}</td>
                        <td>{passenger.age}</td>
                        <td>{humanise(passenger.gender)}</td>
                        <td>{humanise(passenger.passengerType)}</td>
                        <td>{passenger.seatNumber ?? "—"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {(booking.status === "CANCELLED" || booking.status === "REFUNDED") && (
            <div className="notice notice--info mt-3">
              <ShieldAlert size={18} aria-hidden="true" />
              <div>
                <strong>Cancellation recorded</strong>
                <p className="small mb-0">
                  {booking.cancellationReason ?? "No reason was recorded."}
                  {booking.cancelledAt ? ` · ${formatDateTime(booking.cancelledAt)}` : ""}
                  {booking.refundDisplay ? ` · demo refund: ${booking.refundDisplay}` : ""}
                </p>
              </div>
            </div>
          )}
        </div>

        <aside className="sticky-side">
          <div className="panel">
            <div className="spread">
              <span className="muted small">Total</span>
              <strong>{booking.totalDisplay}</strong>
            </div>
            {booking.payment && (
              <>
                <div className="divider" />
                <div className="spread">
                  <span className="muted small">Payment</span>
                  <StatusBadge status={booking.payment.status} />
                </div>
                <p className="small muted mt-1">{booking.payment.message}</p>
                <div className="kv small">
                  <dt>Payment ID</dt>
                  <dd>{booking.payment.id}</dd>
                  <dt>Provider</dt>
                  <dd>{booking.payment.provider}</dd>
                  <dt>Mode</dt>
                  <dd>{booking.payment.mode}</dd>
                  {booking.payment.providerPaymentId && (
                    <>
                      <dt>Provider ref</dt>
                      <dd>{booking.payment.providerPaymentId}</dd>
                    </>
                  )}
                </div>
              </>
            )}

            {isDemoPaymentPending && (
              <div className="mt-3">
                <div className="notice notice--warn">
                  <CreditCard size={17} aria-hidden="true" />
                  <span className="small">
                    Simulated checkout. No card, UPI or bank details are collected and no money moves.
                  </span>
                </div>
                <button
                  type="button"
                  className="btn btn--primary btn--block mt-2"
                  disabled={paying !== null}
                  onClick={() => completeDemoPayment("SUCCESS")}
                >
                  {paying === "SUCCESS" ? "Processing…" : `Pay ${booking.totalDisplay} (simulated)`}
                </button>
                <button
                  type="button"
                  className="btn btn--ghost btn--block mt-1"
                  disabled={paying !== null}
                  onClick={() => completeDemoPayment("FAILURE")}
                >
                  {paying === "FAILURE" ? "Processing…" : "Simulate a failed payment"}
                </button>
              </div>
            )}

            {booking.officialHandoffUrl && (
              <a
                className="btn btn--lime btn--block mt-3"
                href={booking.officialHandoffUrl}
                target="_blank"
                rel="noreferrer noopener"
              >
                Continue on the official channel <ExternalLink size={15} aria-hidden="true" />
              </a>
            )}

            {canCancel && (
              <button type="button" className="btn btn--danger btn--block mt-3" onClick={() => setCancelling(true)}>
                Cancel booking
              </button>
            )}

            <p className="tiny muted mt-2 mb-0">
              Booking reference {booking.id} · {booking.travellerCount} traveller(s) · contact {booking.contactEmail}
            </p>
          </div>
        </aside>
      </div>

      {booking.notes && (
        <div className="notice notice--info mb-4">
          <AlertTriangle size={17} aria-hidden="true" />
          <span className="small">{booking.notes}</span>
        </div>
      )}

      {cancelling && (
        <div className="modal-backdrop" role="dialog" aria-modal="true" aria-label="Cancel booking">
          <div className="modal">
            <h2>Cancel this booking?</h2>
            <p className="small muted">
              The demo refund follows the published policy for this category. Cancelling releases any held seats or
              capacity immediately.
            </p>
            <label className="field">
              <span className="field__label">Reason (optional)</span>
              <textarea
                className="textarea"
                rows={3}
                maxLength={300}
                value={cancelReason}
                onChange={(event) => setCancelReason(event.target.value)}
              />
            </label>
            <div className="row mt-3" style={{ justifyContent: "flex-end" }}>
              <button type="button" className="btn btn--ghost" onClick={() => setCancelling(false)}>
                Keep booking
              </button>
              <button type="button" className="btn btn--danger" onClick={cancelBooking}>
                Yes, cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function categoryIcon(category: string) {
  switch (category) {
    case "TRAIN":
      return TrainFront;
    case "FLIGHT":
      return Plane;
    case "HOLIDAY":
      return BedDouble;
    case "PREMIUM_BUS":
      return Sparkles;
    default:
      return BusFront;
  }
}

function TimelineStep({ title, body, state }: { title: string; body: string; state: "done" | "current" | "pending" }) {
  return (
    <div className={`timeline__step ${state}`}>
      <span className="timeline__dot" aria-hidden="true" />
      <div className="timeline__body">
        <strong>{title}</strong>
        <p className="small muted mb-0">{body}</p>
      </div>
    </div>
  );
}

function BusDetails({ booking }: { booking: Booking }) {
  const bus = booking.details.bus!;
  return (
    <div>
      <h3 className="row" style={{ gap: "0.5rem" }}>
        <BusFront size={18} aria-hidden="true" /> {bus.operatorName}
        {bus.premium && <span className="badge badge--dark">Premium</span>}
      </h3>
      <div className="kv mt-2">
        <dt>Journey</dt>
        <dd>
          {bus.departureCity} → {bus.arrivalCity}
        </dd>
        <dt>Travel date</dt>
        <dd>{formatDateLong(bus.travelDate)}</dd>
        <dt>Departure</dt>
        <dd>{bus.departAt}</dd>
        <dt>Arrival</dt>
        <dd>{bus.arriveAt}</dd>
        <dt>Coach</dt>
        <dd>{bus.busType.replaceAll("_", " ").toLowerCase()}</dd>
        <dt>Seats</dt>
        <dd>{bus.seatNumbers.join(", ")}</dd>
        <dt>Boarding</dt>
        <dd>{bus.boardingPoint}</dd>
        <dt>Dropping</dt>
        <dd>{bus.droppingPoint}</dd>
      </div>
    </div>
  );
}

function TrainDetails({ booking }: { booking: Booking }) {
  const train = booking.details.train!;
  return (
    <div>
      <h3 className="row" style={{ gap: "0.5rem" }}>
        <TrainFront size={18} aria-hidden="true" /> {train.trainNumber} · {train.trainName}
      </h3>
      <div className="notice notice--warn mt-2">
        <AlertTriangle size={17} aria-hidden="true" />
        <span className="small">
          Reservation <strong>request</strong> only. No PNR, seat allocation or ticket exists for this booking.
        </span>
      </div>
      <div className="kv mt-2">
        <dt>Journey date</dt>
        <dd>{formatDateLong(train.journeyDate)}</dd>
        <dt>From</dt>
        <dd>{train.fromStation}</dd>
        <dt>To</dt>
        <dd>{train.toStation}</dd>
        <dt>Class</dt>
        <dd>{train.travelClass.replaceAll("_", " ")}</dd>
        <dt>Quota</dt>
        <dd>{train.quota}</dd>
      </div>
      <a className="btn btn--lime btn--sm mt-2" href={train.officialHandoffUrl} target="_blank" rel="noreferrer noopener">
        Book on official IRCTC <ExternalLink size={14} aria-hidden="true" />
      </a>
    </div>
  );
}

function FlightDetails({ booking }: { booking: Booking }) {
  const flight = booking.details.flight!;
  return (
    <div>
      <h3 className="row" style={{ gap: "0.5rem" }}>
        <Plane size={18} aria-hidden="true" /> {flight.airlineName} {flight.flightNumber}
      </h3>
      <div className="notice notice--warn mt-2">
        <AlertTriangle size={17} aria-hidden="true" />
        <span className="small">
          Demo flight request. The fare was revalidated from sample data; no airline reservation or ticket was created.
        </span>
      </div>
      <div className="kv mt-2">
        <dt>Journey date</dt>
        <dd>{formatDateLong(flight.journeyDate)}</dd>
        <dt>Route</dt>
        <dd>
          {flight.originIata} {flight.departLocal} → {flight.destinationIata} {flight.arriveLocal}
        </dd>
        <dt>Cabin</dt>
        <dd>{flight.cabin}</dd>
        <dt>Stops</dt>
        <dd>{flight.stops === 0 ? "Non-stop" : flight.stops}</dd>
        <dt>Baggage</dt>
        <dd>{flight.baggageNote}</dd>
      </div>
      <p className="tiny muted mt-1 mb-0">{flight.providerConditions}</p>
    </div>
  );
}

function HolidayDetails({ booking }: { booking: Booking }) {
  const holiday = booking.details.holiday!;
  return (
    <div>
      <h3 className="row" style={{ gap: "0.5rem" }}>
        <BedDouble size={18} aria-hidden="true" /> {holiday.packageTitle}
      </h3>
      <img
        src={holiday.imageUrl}
        alt={`Illustration for ${holiday.packageTitle}`}
        style={{ borderRadius: 12, aspectRatio: "16/7", objectFit: "cover", width: "100%", marginTop: "0.8rem" }}
      />
      <div className="kv mt-2">
        <dt>Destination</dt>
        <dd>{holiday.destinationName}</dd>
        <dt>Dates</dt>
        <dd>
          {formatDateLong(holiday.startDate)} → {formatDateLong(holiday.endDate)}
        </dd>
        <dt>Travellers</dt>
        <dd>{holiday.travellers}</dd>
        <dt>Rooms</dt>
        <dd>{holiday.rooms}</dd>
        <dt>Package type</dt>
        <dd>{holiday.landOnly ? "Land-only" : "With transport components"}</dd>
      </div>
      <Link to={`/holidays/${holiday.packageSlug}`} className="btn btn--ghost btn--sm mt-2">
        View itinerary
      </Link>
    </div>
  );
}

export function RefundNote({ booking }: { booking: Booking }) {
  if (!booking.refundDisplay) return null;
  return (
    <div className="notice notice--ok">
      <BadgeCheck size={17} aria-hidden="true" />
      <span>Demo refund recorded: {formatMoney(booking.refundAmountMinor ?? 0, booking.currency)}</span>
    </div>
  );
}
