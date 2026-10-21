import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useFieldArray, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { ArrowRight, ChevronDown, ChevronUp, ExternalLink, Info, TrainFront } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { TrainSearchForm } from "../components/SearchForms";
import { EmptyState, ErrorState, LoadingBlock, ProviderNotice } from "../components/Feedback";
import { StatusBadge } from "../components/Badges";
import { useAsync } from "../hooks/useAsync";
import { useToast } from "../context/ToastContext";
import { api, ApiError, newIdempotencyKey } from "../lib/api";
import { formatDateLong } from "../lib/format";
import type { Booking, ClassAvailability, TrainResult, TrainSearchResponse } from "../lib/types";

const passengerSchema = z.object({
  fullName: z.string().trim().min(2, "Enter the passenger’s full name").max(120),
  age: z.number({ invalid_type_error: "Enter the passenger’s age" }).int().min(0).max(120),
  gender: z.enum(["MALE", "FEMALE", "OTHER"]),
  passengerType: z.enum(["ADULT", "CHILD", "INFANT"]),
});

const bookingSchema = z.object({
  travelClass: z.string().min(1, "Choose a class"),
  quota: z.string().optional(),
  contactName: z.string().trim().min(2, "Enter a contact name").max(120),
  contactEmail: z.string().trim().email("Enter a valid email"),
  contactPhone: z.string().regex(/^$|^[0-9+\- ]{6,20}$/, "Enter a valid phone number").or(z.literal("")),
  passengers: z.array(passengerSchema).min(1),
});

type BookingValues = z.infer<typeof bookingSchema>;

export function TrainPage() {
  const [params] = useSearchParams();
  const from = params.get("from") ?? "";
  const to = params.get("to") ?? "";
  const date = params.get("date") ?? "";
  const passengers = Number(params.get("passengers") ?? 1);
  const hasSearch = Boolean(from && to && date);

  return (
    <div className="container">
      <PageHead
        eyebrow="Indian Railways · sample data"
        title={hasSearch ? "Choose a sample service" : "Trains, with the handoff made clear"}
        lede="Timetables, classes and availability here are sample data for planning. A request creates a reservation request in your account — the actual ticket is issued only on the official IRCTC portal."
        crumbs={[{ to: "/", label: "Home" }, { label: "Trains" }]}
      />

      <div className="panel mb-3">
        <TrainSearchForm
          defaults={{
            from: from || undefined,
            to: to || undefined,
            date: date || undefined,
            passengers: Number.isFinite(passengers) ? passengers : undefined,
          }}
        />
      </div>

      {hasSearch ? (
        <TrainResults from={from} to={to} date={date} passengers={passengers} />
      ) : (
        <div className="grid grid--2 mb-4">
          <div className="card card--pad">
            <span className="avatar" aria-hidden="true">
              <TrainFront size={20} />
            </span>
            <h2 className="mt-2">Planning-first rail search</h2>
            <p className="small muted">
              Compare sample departure times across 29 services and 32 stations, including Shatabdi, Rajdhani, express
              and superfast patterns. Class availability mirrors typical waiting-list behaviour so you can plan
              realistically.
            </p>
          </div>
          <div className="card card--pad">
            <span className="avatar" aria-hidden="true">
              <ExternalLink size={20} />
            </span>
            <h2 className="mt-2">Tickets stay with IRCTC</h2>
            <p className="small muted">
              BookMyTicket never issues a PNR, seat allocation or railway ticket. When you are ready, a one-tap handoff
              opens the official IRCTC search with your route in mind.
            </p>
            <a
              className="link small"
              href="https://www.irctc.co.in/nget/train-search"
              target="_blank"
              rel="noreferrer noopener"
            >
              Open official IRCTC <ExternalLink size={13} aria-hidden="true" />
            </a>
          </div>
        </div>
      )}
    </div>
  );
}

function TrainResults({ from, to, date, passengers }: { from: string; to: string; date: string; passengers: number }) {
  const [bookingTrain, setBookingTrain] = useState<TrainResult | null>(null);
  const { data, loading, error, reload } = useAsync<TrainSearchResponse>(
    (signal) => api.post("/api/trains/search", { from, to, date, passengers }, signal),
    [from, to, date, passengers],
  );

  return (
    <section className="mb-4" aria-label="Train search results">
      <div className="spread mb-2">
        <div>
          <h2 className="mb-0">
            {from} → {to}
          </h2>
          <p className="small muted mb-0">
            {formatDateLong(date)} · {passengers} {passengers === 1 ? "passenger" : "passengers"}
            {data ? ` · ${data.trains.length} sample services` : ""}
          </p>
        </div>
      </div>

      {data && (
        <ProviderNotice
          mode={data.providerMode}
          message={data.providerMessage}
          sourceTimestamp={data.sourceTimestamp}
          ticketNote="Tickets are issued only via the official IRCTC portal."
        />
      )}

      {loading && <LoadingBlock label="Looking up sample services" />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && data && data.trains.length === 0 && (
        <EmptyState title="No sample services on this route" icon={<TrainFront size={30} aria-hidden="true" />}>
          <p>
            Try a major station pair such as MAS–CBE, MAS–SBC, MAS–NDLS or CSMT–PUNE. The {data.dataNote}
          </p>
        </EmptyState>
      )}

      {!loading &&
        data?.trains.map((train) => (
          <TrainCard key={train.id} train={train} onBook={() => setBookingTrain(train)} />
        ))}

      {bookingTrain && data && (
        <TrainBookingModal
          train={bookingTrain}
          date={date}
          passengers={passengers}
          quotas={data.quotas}
          onClose={() => setBookingTrain(null)}
        />
      )}
    </section>
  );
}

function TrainCard({ train, onBook }: { train: TrainResult; onBook: () => void }) {
  const [showRoute, setShowRoute] = useState(false);
  const bookable = train.classes.filter((clazz) => clazz.fareMinor != null);

  return (
    <article className="card card--pad mt-2">
      <div className="spread">
        <div>
          <div className="row" style={{ gap: "0.5rem" }}>
            <span className="avatar" aria-hidden="true">
              <TrainFront size={19} />
            </span>
            <div>
              <strong>
                {train.trainNumber} · {train.name}
              </strong>
              <div className="tiny muted">
                Runs {train.runningDays === "DAILY" ? "daily" : `on ${train.runningDays}`}
                {train.distanceKm ? ` · ${train.distanceKm} km sample route` : ""}
              </div>
            </div>
          </div>
          <div className="times mt-2">
            <div>
              <div className="times__h">{train.departTime}</div>
              <div className="times__meta">{train.fromName}</div>
            </div>
            <div className="times__line" aria-hidden="true" />
            <div>
              <div className="times__h">{train.arriveTime}</div>
              <div className="times__meta">
                {train.toName}
                {train.arriveDayOffset > 0 ? ` (+${train.arriveDayOffset}d)` : ""}
              </div>
            </div>
            <div className="tiny muted">{train.durationLabel}</div>
          </div>
        </div>
        <div className="fare">
          {!train.runsOnDate && <span className="badge badge--warn">Does not run on this date</span>}
          <button type="button" className="btn btn--primary btn--sm mt-2" onClick={onBook} disabled={bookable.length === 0}>
            Request booking
          </button>
        </div>
      </div>

      <div className="mt-2">
        {train.classes.map((clazz) => (
          <ClassRow key={clazz.code} clazz={clazz} />
        ))}
      </div>

      <button type="button" className="btn btn--ghost btn--sm mt-2" onClick={() => setShowRoute((value) => !value)}>
        {showRoute ? <ChevronUp size={15} aria-hidden="true" /> : <ChevronDown size={15} aria-hidden="true" />} Sample route
        stops
      </button>
      {showRoute && (
        <div className="table-wrap mt-2">
          <table className="table">
            <thead>
              <tr>
                <th>Station</th>
                <th>Arrives</th>
                <th>Departs</th>
                <th>Day</th>
                <th>Distance</th>
              </tr>
            </thead>
            <tbody>
              {train.routeStops.map((stop) => (
                <tr key={`${stop.stationCode}-${stop.dayNumber}-${stop.departTime}`}>
                  <td>
                    {stop.stationName} <span className="muted">({stop.stationCode})</span>
                  </td>
                  <td>{stop.arriveTime}</td>
                  <td>{stop.departTime}</td>
                  <td>{stop.dayNumber}</td>
                  <td>{stop.distanceKm != null ? `${stop.distanceKm} km` : "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </article>
  );
}

function ClassRow({ clazz }: { clazz: ClassAvailability }) {
  return (
    <div className="class-row">
      <div>
        <strong>{clazz.label}</strong>
        {clazz.note && <div className="tiny muted">{clazz.note}</div>}
      </div>
      <div>
        <StatusBadge status={clazz.availabilityStatus} />
        <div className="tiny muted mt-1">
          {clazz.availabilityStatus === "WAITLIST" && clazz.wlPosition != null
            ? `Waitlist position ${clazz.wlPosition} (sample)`
            : clazz.availableSeats != null
              ? `${clazz.availableSeats} sample seats`
              : "Availability sample"}
        </div>
      </div>
      <div className="fare">
        <div className="fare__amount">{clazz.fareDisplay ?? "—"}</div>
        <div className="tiny muted">per passenger</div>
      </div>
    </div>
  );
}

function TrainBookingModal({
  train,
  date,
  passengers,
  quotas,
  onClose,
}: {
  train: TrainResult;
  date: string;
  passengers: number;
  quotas: { code: string; label: string }[];
  onClose: () => void;
}) {
  const navigate = useNavigate();
  const { notify } = useToast();
  const [acceptHandoff, setAcceptHandoff] = useState(false);
  const bookable = train.classes.filter((clazz) => clazz.fareMinor != null);
  const idempotencyRef = useState(() => newIdempotencyKey())[0];

  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<BookingValues>({
    resolver: zodResolver(bookingSchema),
    defaultValues: {
      travelClass: bookable[0]?.code ?? "",
      quota: "GENERAL",
      contactName: "",
      contactEmail: "",
      contactPhone: "",
      passengers: Array.from({ length: Math.max(1, Math.min(passengers, 6)) }, () => ({
        fullName: "",
        age: 30,
        gender: "MALE" as const,
        passengerType: "ADULT" as const,
      })),
    },
  });
  const { fields } = useFieldArray({ control, name: "passengers" });

  const onSubmit = async (values: BookingValues) => {
    try {
      const booking = await api.post<Booking>("/api/bookings/train", {
        serviceId: train.id,
        travelClass: values.travelClass,
        quota: values.quota,
        journeyDate: date,
        fromStation: train.fromName,
        toStation: train.toName,
        contactName: values.contactName,
        contactEmail: values.contactEmail,
        contactPhone: values.contactPhone,
        passengers: values.passengers,
        idempotencyKey: idempotencyRef,
      });
      notify("Reservation request created. No PNR or ticket has been issued.", "ok");
      navigate(`/bookings/${booking.id}`);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 401) {
          navigate(`/login?redirect=${encodeURIComponent(`/trains?from=${train.fromCode}&to=${train.toCode}&date=${date}&passengers=${passengers}`)}`);
          return;
        }
        notify(err.message, "error");
        return;
      }
      notify("We couldn’t create the reservation request.", "error");
    }
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-label="Request a train booking">
      <div className="modal">
        <div className="spread mb-2">
          <h2 className="mb-0">
            {train.trainNumber} · {train.name}
          </h2>
          <button type="button" className="btn btn--ghost btn--sm" onClick={onClose}>
            Close
          </button>
        </div>
        <div className="notice notice--warn mb-2">
          <Info size={18} aria-hidden="true" />
          <span className="small">
            This creates a reservation <strong>request</strong> in your account. It is not a ticket: no PNR, no seat
            allocation and no payment. Complete the journey booking on the official IRCTC portal.
          </span>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="grid grid--3">
            <label className="field">
              <span className="field__label">Class</span>
              <select className="select" {...register("travelClass")}>
                {bookable.map((clazz) => (
                  <option key={clazz.code} value={clazz.code}>
                    {clazz.label} — {clazz.fareDisplay}
                  </option>
                ))}
              </select>
              {errors.travelClass && <span className="field__error">{errors.travelClass.message}</span>}
            </label>
            <label className="field">
              <span className="field__label">Quota</span>
              <select className="select" {...register("quota")}>
                {quotas.map((quota) => (
                  <option key={quota.code} value={quota.code}>
                    {quota.label}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              <span className="field__label">Journey date</span>
              <input className="input" value={formatDateLong(date)} readOnly />
            </label>
          </div>

          <div className="divider" />
          <div className="grid grid--3">
            <label className="field">
              <span className="field__label">Contact name</span>
              <input className="input" {...register("contactName")} />
              {errors.contactName && <span className="field__error">{errors.contactName.message}</span>}
            </label>
            <label className="field">
              <span className="field__label">Email</span>
              <input className="input" type="email" {...register("contactEmail")} />
              {errors.contactEmail && <span className="field__error">{errors.contactEmail.message}</span>}
            </label>
            <label className="field">
              <span className="field__label">Phone (optional)</span>
              <input className="input" {...register("contactPhone")} />
              {errors.contactPhone && <span className="field__error">{errors.contactPhone.message}</span>}
            </label>
          </div>

          <div className="divider" />
          <h3>Passengers</h3>
          <div className="stack--sm">
            {fields.map((field, index) => (
              <div key={field.id} className="grid grid--3">
                <label className="field">
                  <span className="field__label">Passenger {index + 1} name</span>
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
                    {...register(`passengers.${index}.age` as const, { valueAsNumber: true })}
                  />
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
            ))}
          </div>

          <label className="checkbox mt-3">
            <input type="checkbox" checked={acceptHandoff} onChange={(event) => setAcceptHandoff(event.target.checked)} />
            <span className="small">
              I understand this is a sample reservation request and that the actual ticket will be booked separately on
              the official IRCTC portal.
            </span>
          </label>

          <button type="submit" className="btn btn--primary btn--block mt-3" disabled={!acceptHandoff || isSubmitting}>
            {isSubmitting ? "Creating request…" : "Create reservation request"}
          </button>
        </form>

        <a
          className="link small row mt-2"
          href="https://www.irctc.co.in/nget/train-search"
          target="_blank"
          rel="noreferrer noopener"
          style={{ justifyContent: "center" }}
        >
          Complete on official IRCTC now <ArrowRight size={14} aria-hidden="true" />
        </a>
      </div>
    </div>
  );
}
