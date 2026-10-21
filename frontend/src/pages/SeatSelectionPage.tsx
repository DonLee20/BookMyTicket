import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useFieldArray, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { ArrowLeft, Armchair, Info, Lock, ShieldCheck } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { ErrorState, LoadingBlock, ProviderNotice } from "../components/Feedback";
import { DemoBadge } from "../components/Badges";
import { useAsync } from "../hooks/useAsync";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { api, ApiError, newIdempotencyKey } from "../lib/api";
import { formatDateLong, formatMoney } from "../lib/format";
import type { Booking, SeatMapResponse, SeatView } from "../lib/types";

const MAX_SEATS = 6;

const passengerSchema = z.object({
  fullName: z.string().trim().min(2, "Enter the passenger’s full name").max(120),
  age: z.number({ invalid_type_error: "Enter the passenger’s age" }).int().min(0, "Enter a valid age").max(120, "Enter a valid age"),
  gender: z.enum(["MALE", "FEMALE", "OTHER"]),
  passengerType: z.enum(["ADULT", "CHILD", "INFANT"]),
});

const checkoutSchema = z.object({
  boardingPoint: z.string().min(1, "Choose a boarding point"),
  droppingPoint: z.string().min(1, "Choose a dropping point"),
  contactName: z.string().trim().min(2, "Enter a contact name").max(120),
  contactEmail: z.string().trim().email("Enter a valid email"),
  contactPhone: z
    .string()
    .regex(/^$|^[0-9+\- ]{6,20}$/, "Enter a valid phone number")
    .or(z.literal("")),
  passengers: z.array(passengerSchema).min(1),
});

type CheckoutValues = z.infer<typeof checkoutSchema>;

export function SeatSelectionPage() {
  const { tripId } = useParams();
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { notify } = useToast();
  const [selected, setSelected] = useState<SeatView[]>([]);
  const idempotencyRef = useRef(newIdempotencyKey());

  const { data, loading, error, reload } = useAsync<SeatMapResponse>(
    (signal) => api.get(`/api/buses/trips/${tripId}/seats`, signal),
    [tripId],
  );

  const trip = data?.trip;
  const seatMap = data?.seatMap;
  const passengersParam = Number(params.get("passengers") ?? 1);

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CheckoutValues>({
    resolver: zodResolver(checkoutSchema),
    defaultValues: {
      boardingPoint: "",
      droppingPoint: "",
      contactName: user?.name ?? "",
      contactEmail: user?.email ?? "",
      contactPhone: user?.phone ?? "",
      passengers: [],
    },
  });

  const { fields } = useFieldArray({ control, name: "passengers" });

  useEffect(() => {
    reset((current) => ({
      ...current,
      boardingPoint: current.boardingPoint || trip?.boardingPoints[0] || "",
      droppingPoint: current.droppingPoint || trip?.droppingPoints[0] || "",
      passengers: selected.map((_seat, index) => ({
        fullName: current.passengers[index]?.fullName ?? "",
        age: current.passengers[index]?.age ?? 30,
        gender: current.passengers[index]?.gender ?? "MALE",
        passengerType: current.passengers[index]?.passengerType ?? "ADULT",
      })),
    }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected.length, trip?.id]);

  useEffect(() => {
    if (user) {
      reset((current) => ({
        ...current,
        contactName: current.contactName || user.name,
        contactEmail: current.contactEmail || user.email,
        contactPhone: current.contactPhone || user.phone || "",
      }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  const toggleSeat = (seat: SeatView) => {
    if (seat.status !== "AVAILABLE") return;
    setSelected((current) => {
      const exists = current.some((item) => item.id === seat.id);
      if (exists) return current.filter((item) => item.id !== seat.id);
      if (current.length >= MAX_SEATS) {
        notify(`You can select up to ${MAX_SEATS} seats in one booking.`, "error");
        return current;
      }
      if (current.length >= passengersParam && passengersParam > 0 && current.length + 1 > passengersParam) {
        notify(`You searched for ${passengersParam} traveller(s). Remove a seat or search again for more.`, "error");
        return current;
      }
      idempotencyRef.current = newIdempotencyKey();
      return [...current, seat];
    });
  };

  const totalMinor = useMemo(
    () => selected.reduce((sum, seat) => sum + seat.priceMinor, 0),
    [selected],
  );

  const onSubmit = async (values: CheckoutValues) => {
    if (!trip) return;
    if (selected.length === 0) {
      notify("Select at least one seat before continuing.", "error");
      return;
    }
    if (!user) {
      const redirect = encodeURIComponent(`/buses/${trip.id}/seats`);
      navigate(`/login?redirect=${redirect}`);
      return;
    }
    try {
      const booking = await api.post<Booking>("/api/bookings/bus", {
        tripId: trip.id,
        seatIds: selected.map((seat) => seat.id),
        boardingPoint: values.boardingPoint,
        droppingPoint: values.droppingPoint,
        contactName: values.contactName,
        contactEmail: values.contactEmail,
        contactPhone: values.contactPhone,
        passengers: values.passengers,
        expectedTotalMinor: totalMinor,
        idempotencyKey: idempotencyRef.current,
      });
      notify("Seats held. Complete the demo payment to confirm.", "ok");
      navigate(`/bookings/${booking.id}`);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          notify(err.message, "error");
          setSelected([]);
          idempotencyRef.current = newIdempotencyKey();
          reload();
          return;
        }
        if (err.status === 401) {
          navigate(`/login?redirect=${encodeURIComponent(`/buses/${trip.id}/seats`)}`);
          return;
        }
        notify(err.message, "error");
        return;
      }
      notify("We couldn’t create the booking. Please try again.", "error");
    }
  };

  if (loading) {
    return (
      <div className="container section">
        <LoadingBlock label="Loading the seat map" />
      </div>
    );
  }

  if (error || !data || !trip || !seatMap) {
    return (
      <div className="container section">
        <ErrorState message={error ?? "This seat map is unavailable."} onRetry={reload} />
        <div className="mt-3">
          <Link to="/buses" className="btn btn--ghost">
            <ArrowLeft size={16} aria-hidden="true" /> Back to bus search
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <PageHead
        eyebrow="Seat selection"
        title={`${trip.operatorName} · ${trip.busTypeLabel}`}
        lede={`${trip.departureCity} → ${trip.arrivalCity} on ${formatDateLong(trip.journeyDate)} · departs ${trip.departTime}`}
        crumbs={[
          { to: "/", label: "Home" },
          { to: "/buses", label: "Buses" },
          { label: "Select seats" },
        ]}
        actions={<DemoBadge mode={seatMap.providerMode} />}
      />

      <ProviderNotice
        mode={seatMap.providerMode}
        message={`${seatMap.layoutLabel} · ${seatMap.availableCount} of ${seatMap.totalSeats} seats open. Seats are held for 20 minutes once a booking is created.`}
      />

      <div className="stepper mt-3">
        <span className="stepper__item active">
          <span className="stepper__num">1</span> Choose seats
        </span>
        <span className="stepper__item">
          <span className="stepper__num">2</span> Travellers &amp; contact
        </span>
        <span className="stepper__item">
          <span className="stepper__num">3</span> Demo payment
        </span>
      </div>

      <div className="sidebar-layout mb-4">
        <div>
          <div className="card card--pad">
            <div className="spread mb-2">
              <h2 className="mb-0">Seat map</h2>
              <div className="seat-legend">
                <span>
                  <i className="selected" aria-hidden="true" /> Selected
                </span>
                <span>
                  <i aria-hidden="true" /> Available
                </span>
                <span>
                  <i className="booked" aria-hidden="true" /> Booked / held
                </span>
              </div>
            </div>
            <div className="seatmap">
              {seatMap.decks.map((deck) => (
                <div key={deck.deck} className="deck">
                  <div className="deck__title">
                    {deck.deckLabel} · {seatMap.layoutLabel}
                  </div>
                  {deck.rows.map((row) => (
                    <div key={row.rowNo} className="seat-row">
                      <span className="seat-row__label">{row.rowNo}</span>
                      {row.seats.map((seat, index) => {
                        const isSelected = selected.some((item) => item.id === seat.id);
                        const isAvailable = seat.status === "AVAILABLE";
                        const gap = deck.rows[0].seats.length > 3 && index === Math.ceil(deck.rows[0].seats.length / 2) - 1;
                        return (
                          <span key={seat.id} style={{ display: "inline-flex", gap: "0.5rem" }}>
                            <button
                              type="button"
                              className={`seat${isSelected ? " seat--selected" : ""}${!isAvailable ? " seat--booked" : ""}`}
                              aria-pressed={isSelected}
                              aria-label={`Seat ${seat.label}, ${isAvailable ? seat.priceDisplay : "unavailable"}, ${
                                seat.type === "SLEEPER" ? "sleeper" : "seater"
                              }${seat.window ? ", window" : ""}`}
                              disabled={!isAvailable}
                              onClick={() => toggleSeat(seat)}
                            >
                              {seat.label}
                            </button>
                            {gap && <span style={{ width: "1.2rem" }} aria-hidden="true" />}
                          </span>
                        );
                      })}
                    </div>
                  ))}
                </div>
              ))}
            </div>
            <p className="tiny muted mt-2 mb-0">
              Window seats are marked in blue. Demo inventory uses generated occupancy — booked seats are sample data,
              not a real operator reservation.
            </p>
          </div>

          <form className="card card--pad mt-3" onSubmit={handleSubmit(onSubmit)} noValidate id="checkout">
            <h2>Travellers &amp; contact</h2>
            <p className="small muted">
              Names are used only for this booking request. For demo inventory, no real ticket or traveller record is
              shared with an operator.
            </p>

            {selected.length > 0 && (
              <div className="pill-group mb-2">
                {selected.map((seat) => (
                  <span key={seat.id} className="chip chip--static">
                    <Armchair size={13} aria-hidden="true" /> {seat.label}
                    <button
                      type="button"
                      className="btn btn--ghost btn--sm"
                      style={{ padding: "0 0.35rem" }}
                      onClick={() => toggleSeat(seat)}
                      aria-label={`Remove seat ${seat.label}`}
                    >
                      ✕
                    </button>
                  </span>
                ))}
              </div>
            )}

            <div className="grid grid--2">
              <label className="field">
                <span className="field__label">Boarding point</span>
                <select className="select" aria-invalid={Boolean(errors.boardingPoint)} {...register("boardingPoint")}>
                  <option value="">Select boarding point</option>
                  {trip.boardingPoints.map((point) => (
                    <option key={point} value={point}>
                      {point}
                    </option>
                  ))}
                </select>
                {errors.boardingPoint && <span className="field__error">{errors.boardingPoint.message}</span>}
              </label>
              <label className="field">
                <span className="field__label">Dropping point</span>
                <select className="select" aria-invalid={Boolean(errors.droppingPoint)} {...register("droppingPoint")}>
                  <option value="">Select dropping point</option>
                  {trip.droppingPoints.map((point) => (
                    <option key={point} value={point}>
                      {point}
                    </option>
                  ))}
                </select>
                {errors.droppingPoint && <span className="field__error">{errors.droppingPoint.message}</span>}
              </label>
            </div>

            <div className="divider" />
            <h3>Contact details</h3>
            <div className="grid grid--3">
              <label className="field">
                <span className="field__label">Full name</span>
                <input className="input" aria-invalid={Boolean(errors.contactName)} {...register("contactName")} />
                {errors.contactName && <span className="field__error">{errors.contactName.message}</span>}
              </label>
              <label className="field">
                <span className="field__label">Email</span>
                <input className="input" type="email" aria-invalid={Boolean(errors.contactEmail)} {...register("contactEmail")} />
                {errors.contactEmail && <span className="field__error">{errors.contactEmail.message}</span>}
              </label>
              <label className="field">
                <span className="field__label">Phone (optional)</span>
                <input className="input" aria-invalid={Boolean(errors.contactPhone)} {...register("contactPhone")} />
                {errors.contactPhone && <span className="field__error">{errors.contactPhone.message}</span>}
              </label>
            </div>

            <div className="divider" />
            <h3>Passengers</h3>
            {fields.length === 0 && <p className="small muted">Select seats above and one passenger row will be created per seat.</p>}
            <div className="stack--sm">
              {fields.map((field, index) => (
                <fieldset key={field.id} className="deck">
                  <legend className="deck__title">
                    Passenger {index + 1} · Seat {selected[index]?.label ?? "—"}
                  </legend>
                  <div className="grid grid--3">
                    <label className="field">
                      <span className="field__label">Full name</span>
                      <input className="input" {...register(`passengers.${index}.fullName` as const)} />
                      {errors.passengers?.[index]?.fullName && (
                        <span className="field__error">{errors.passengers[index]?.fullName?.message}</span>
                      )}
                    </label>
                    <label className="field">
                      <span className="field__label">Age</span>
                      <input
                        type="number"
                        className="input"
                        min={0}
                        max={120}
                        {...register(`passengers.${index}.age` as const, { valueAsNumber: true })}
                      />
                      {errors.passengers?.[index]?.age && (
                        <span className="field__error">{errors.passengers[index]?.age?.message}</span>
                      )}
                    </label>
                    <label className="field">
                      <span className="field__label">Gender</span>
                      <select className="select" {...register(`passengers.${index}.gender` as const)}>
                        <option value="MALE">Male</option>
                        <option value="FEMALE">Female</option>
                        <option value="OTHER">Other</option>
                      </select>
                    </label>
                  </div>
                </fieldset>
              ))}
            </div>

            <div className="honesty-strip mt-3">
              <ShieldCheck size={17} aria-hidden="true" />
              <span>
                This creates a demo booking against sample inventory. Seats are held for 20 minutes; confirm with the
                simulated payment step on the next screen. Nothing is charged and no real seat is reserved.
              </span>
            </div>

            <button type="submit" className="btn btn--primary btn--lg btn--block mt-3" disabled={isSubmitting || selected.length === 0}>
              {isSubmitting ? "Creating booking…" : `Continue with ${selected.length || 0} seat${selected.length === 1 ? "" : "s"}`}
            </button>
            {!user && (
              <p className="small muted center mt-1 mb-0">
                You’ll be asked to sign in so the booking appears under your account.
              </p>
            )}
          </form>
        </div>

        <aside className="sticky-side">
          <div className="panel">
            <h3 className="mb-1">Your selection</h3>
            <div className="summary-row">
              <span className="muted">Coach</span>
              <span>{trip.busTypeLabel}</span>
            </div>
            <div className="summary-row">
              <span className="muted">Journey</span>
              <span>{formatDateLong(trip.journeyDate)}</span>
            </div>
            <div className="summary-row">
              <span className="muted">Departure</span>
              <span>
                {trip.departTime} · {trip.departureCity}
              </span>
            </div>
            <div className="summary-row">
              <span className="muted">Seats</span>
              <span>{selected.length > 0 ? selected.map((seat) => seat.label).join(", ") : "None selected"}</span>
            </div>
            <div className="summary-row summary-row--total">
              <span>Total</span>
              <span>{formatMoney(totalMinor, seatMap.currency)}</span>
            </div>
            <div className="tiny muted mt-1">
              {seatMap.pricePerSeatDisplay} per seat · demo fare, taxes included in the sample price
            </div>
            <div className="notice notice--info mt-2">
              <Info size={16} aria-hidden="true" />
              <span className="tiny">
                Seat locks are enforced server-side with pessimistic locking, so two travellers cannot hold the same
                seat.
              </span>
            </div>
            <div className="row tiny muted mt-2">
              <Lock size={13} aria-hidden="true" /> Held for 20 minutes after booking creation.
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}
