import { useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useFieldArray, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { AlertTriangle, ExternalLink, Plane, PlaneLanding, PlaneTakeoff } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { FlightSearchForm } from "../components/SearchForms";
import { EmptyState, ErrorState, LoadingBlock, ProviderNotice } from "../components/Feedback";
import { DemoBadge } from "../components/Badges";
import { useAsync } from "../hooks/useAsync";
import { useToast } from "../context/ToastContext";
import { api, ApiError, newIdempotencyKey } from "../lib/api";
import { formatDateLong, formatDuration, formatMoney } from "../lib/format";
import type { Booking, FlightOffer, FlightSearchResponse } from "../lib/types";

const passengerSchema = z.object({
  fullName: z.string().trim().min(2, "Enter the passenger’s full name").max(120),
  age: z.number({ invalid_type_error: "Enter the passenger’s age" }).int().min(0).max(120),
  gender: z.enum(["MALE", "FEMALE", "OTHER"]),
  passengerType: z.enum(["ADULT", "CHILD", "INFANT"]),
});

const bookingSchema = z.object({
  contactName: z.string().trim().min(2, "Enter a contact name").max(120),
  contactEmail: z.string().trim().email("Enter a valid email"),
  contactPhone: z.string().regex(/^$|^[0-9+\- ]{6,20}$/, "Enter a valid phone number").or(z.literal("")),
  passengers: z.array(passengerSchema).min(1),
});

type BookingValues = z.infer<typeof bookingSchema>;
type FlightSort = "price" | "departure" | "duration";

export function FlightPage() {
  const [params] = useSearchParams();
  const origin = params.get("origin") ?? "";
  const destination = params.get("destination") ?? "";
  const date = params.get("date") ?? "";
  const cabin = params.get("cabin") ?? "ECONOMY";
  const adults = Number(params.get("adults") ?? 1);
  const hasSearch = Boolean(origin && destination && date);

  return (
    <div className="container">
      <PageHead
        eyebrow="Global flights · demo fares"
        title={hasSearch ? "Choose an offer" : "Forty-five airports, one clear caveat"}
        lede="Search demo flight offers across domestic India and international routes. Fares are revalidated before a booking request is created. We do not issue tickets; every request is clearly marked as demo."
        crumbs={[{ to: "/", label: "Home" }, { label: "Flights" }]}
      />

      <div className="panel mb-3">
        <FlightSearchForm
          defaults={{
            origin: origin || undefined,
            destination: destination || undefined,
            date: date || undefined,
            cabin,
            adults: Number.isFinite(adults) ? adults : undefined,
          }}
        />
      </div>

      {hasSearch ? (
        <FlightResults origin={origin} destination={destination} date={date} cabin={cabin} adults={adults} />
      ) : (
        <div className="grid grid--3 mb-4">
          {[
            { icon: PlaneTakeoff, title: "Domestic first", body: "Chennai to Bengaluru, Delhi, Mumbai, Kochi, Hyderabad and more — 16 sample IndiWings and SkyDeccan services." },
            { icon: PlaneLanding, title: "Short-haul international", body: "Dubai, Abu Dhabi, Doha, Muscat, Singapore, Kuala Lumpur, Bangkok and Colombo with sample fares." },
            { icon: Plane, title: "Long-haul samples", body: "London, Paris, Frankfurt, Amsterdam, Tokyo, New York, Sydney and Melbourne sample offers." },
          ].map(({ icon: Icon, title, body }) => (
            <div key={title} className="card card--pad">
              <span className="avatar" aria-hidden="true">
                <Icon size={19} />
              </span>
              <h3 className="mt-2">{title}</h3>
              <p className="small muted mb-0">{body}</p>
            </div>
          ))}
        </div>
      )}

      {!hasSearch && (
        <div className="notice notice--warn mb-4">
          <AlertTriangle size={18} aria-hidden="true" />
          <div>
            <strong>No ticketing, no invented live fares.</strong>
            <p className="small mb-0">
              All fares on this page are generated demo offers. A booking request records the offer you selected and
              confirms the sample price; it does not reserve an airline seat or issue a ticket.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}

function FlightResults({
  origin,
  destination,
  date,
  cabin,
  adults,
}: {
  origin: string;
  destination: string;
  date: string;
  cabin: string;
  adults: number;
}) {
  const [selected, setSelected] = useState<FlightOffer | null>(null);
  const [sort, setSort] = useState<FlightSort>("price");
  const { data, loading, error, reload } = useAsync<FlightSearchResponse>(
    (signal) =>
      api.post(
        "/api/flights/search",
        {
          legs: [{ origin, destination, date }],
          cabin,
          adults: Number.isFinite(adults) ? adults : 1,
          children: 0,
          infants: 0,
        },
        signal,
      ),
    [origin, destination, date, cabin, adults],
  );

  const offers = useMemo(() => {
    const all = data?.legs[0]?.offers ?? [];
    return [...all].sort((a, b) => {
      if (sort === "departure") return a.departLocal.localeCompare(b.departLocal);
      if (sort === "duration") return a.durationMinutes - b.durationMinutes;
      return a.pricePerTravellerMinor - b.pricePerTravellerMinor;
    });
  }, [data, sort]);

  const leg = data?.legs[0];

  return (
    <section className="mb-4" aria-label="Flight search results">
      <div className="spread mb-2">
        <div>
          <h2 className="mb-0">
            {leg ? `${leg.originCity} (${origin}) → ${leg.destinationCity} (${destination})` : `${origin} → ${destination}`}
          </h2>
          <p className="small muted mb-0">
            {formatDateLong(date)} · {adults} {adults === 1 ? "adult" : "adults"} · {cabin.replaceAll("_", " ").toLowerCase()}
            {data ? ` · ${offers.length} demo offers` : ""}
          </p>
        </div>
        <label className="row small" style={{ gap: "0.4rem" }}>
          <span className="muted">Sort</span>
          <select className="select" style={{ width: "auto" }} value={sort} onChange={(event) => setSort(event.target.value as FlightSort)}>
            <option value="price">Lowest fare</option>
            <option value="departure">Departure</option>
            <option value="duration">Duration</option>
          </select>
        </label>
      </div>

      {data && (
        <ProviderNotice mode={data.providerMode} message={data.providerMessage} sourceTimestamp={data.sourceTimestamp} />
      )}

      {loading && <LoadingBlock label="Searching demo flight offers" />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && data && offers.length === 0 && (
        <EmptyState title="No sample offers on this route" icon={<Plane size={30} aria-hidden="true" />}>
          <p>
            Try Chennai (MAA) to Bengaluru (BLR) or Dubai (DXB), or another cabin such as Business. Sample schedules
            cover 74 directional services.
          </p>
          <Link to="/flights" className="btn btn--ghost btn--sm mt-2">
            Change search
          </Link>
        </EmptyState>
      )}

      {!loading &&
        offers.map((offer) => (
          <article key={offer.offerRef} className="card card--pad mt-2">
            <div className="spread">
              <div>
                <div className="row" style={{ gap: "0.5rem" }}>
                  <span className="avatar" aria-hidden="true">
                    {offer.airlineCode}
                  </span>
                  <div>
                    <strong>
                      {offer.airlineName} · {offer.flightNumber}
                    </strong>
                    <div className="tiny muted">
                      {offer.aircraft} · {offer.stops === 0 ? "Non-stop" : `${offer.stops} stop(s)`} ·{" "}
                      {offer.cabinLabel}
                    </div>
                  </div>
                  <DemoBadge mode={offer.providerMode} />
                </div>
                <div className="times mt-2">
                  <div>
                    <div className="times__h">{offer.departLocal}</div>
                    <div className="times__meta">
                      {offer.originIata} · {offer.originCity}
                    </div>
                  </div>
                  <div className="times__line" aria-hidden="true" />
                  <div>
                    <div className="times__h">{offer.arriveLocal}</div>
                    <div className="times__meta">
                      {offer.destinationIata} · {offer.destinationCity}
                    </div>
                  </div>
                  <div className="tiny muted">{formatDuration(offer.durationMinutes)}</div>
                </div>
                <div className="row row--wrap mt-2">
                  <span className="badge">{offer.baggageNote}</span>
                  <span className={`badge ${offer.refundable ? "badge--ok" : "badge--warn"}`}>
                    {offer.refundable ? "Refundable sample fare" : "Non-refundable sample fare"}
                  </span>
                  {offer.seatsLeft != null && <span className="badge">{offer.seatsLeft} sample seats left</span>}
                </div>
              </div>
              <div className="fare">
                <div className="tiny muted">per adult, total</div>
                <div className="fare__amount">{offer.priceDisplay}</div>
                <div className="tiny muted mb-2">
                  {adults > 1 ? `${formatMoney(offer.totalPriceMinor, offer.currency)} for ${adults} adults` : "incl. sample taxes"}
                </div>
                <button type="button" className="btn btn--primary btn--sm" onClick={() => setSelected(offer)}>
                  Request this fare
                </button>
              </div>
            </div>
            <p className="tiny muted mt-2 mb-0">{offer.fareConditions}</p>
          </article>
        ))}

      {selected && (
        <FlightBookingModal
          offer={selected}
          adults={adults}
          onClose={() => setSelected(null)}
          onPriceChanging={() => reload()}
        />
      )}
    </section>
  );
}

function FlightBookingModal({
  offer,
  adults,
  onClose,
  onPriceChanging,
}: {
  offer: FlightOffer;
  adults: number;
  onClose: () => void;
  onPriceChanging: () => void;
}) {
  const navigate = useNavigate();
  const { notify } = useToast();
  const [accepted, setAccepted] = useState(false);
  const [priceChangeMessage, setPriceChangeMessage] = useState<string | null>(null);
  const idempotencyRef = useState(() => newIdempotencyKey())[0];

  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<BookingValues>({
    resolver: zodResolver(bookingSchema),
    defaultValues: {
      contactName: "",
      contactEmail: "",
      contactPhone: "",
      passengers: Array.from({ length: Math.max(1, Math.min(adults, 9)) }, () => ({
        fullName: "",
        age: 30,
        gender: "MALE" as const,
        passengerType: "ADULT" as const,
      })),
    },
  });
  const { fields } = useFieldArray({ control, name: "passengers" });

  const submit = async (values: BookingValues, acceptPriceChange: boolean) => {
    try {
      const booking = await api.post<Booking>("/api/bookings/flight", {
        offerRef: offer.offerRef,
        acceptPriceChange,
        contactName: values.contactName,
        contactEmail: values.contactEmail,
        contactPhone: values.contactPhone,
        passengers: values.passengers,
        idempotencyKey: idempotencyRef,
      });
      notify("Flight request created. No ticket has been issued.", "ok");
      navigate(`/bookings/${booking.id}`);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 401) {
          navigate(`/login?redirect=${encodeURIComponent(`/flights?origin=${offer.originIata}&destination=${offer.destinationIata}&date=${offer.journeyDate}`)}`);
          return;
        }
        if (err.code === "OFFER_PRICE_CHANGED") {
          setPriceChangeMessage(err.message);
          onPriceChanging();
          return;
        }
        notify(err.message, "error");
        return;
      }
      notify("We couldn’t create the flight request.", "error");
    }
  };

  const onSubmit = (values: BookingValues) => {
    if (!accepted) {
      notify("Please acknowledge that this is a demo request with no ticket issuance.", "error");
      return;
    }
    return submit(values, Boolean(priceChangeMessage));
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-label="Request this flight fare">
      <div className="modal">
        <div className="spread mb-2">
          <h2 className="mb-0">
            {offer.airlineName} {offer.flightNumber}
          </h2>
          <button type="button" className="btn btn--ghost btn--sm" onClick={onClose}>
            Close
          </button>
        </div>
        <p className="small muted">
          {offer.originIata} {offer.departLocal} → {offer.destinationIata} {offer.arriveLocal} · {formatDateLong(offer.journeyDate)} ·{" "}
          {offer.cabinLabel}
        </p>

        <div className="notice notice--warn mb-2">
          <AlertTriangle size={18} aria-hidden="true" />
          <span className="small">
            This provider is in demo mode. The fare is a generated sample, revalidated when you submit. No airline seat
            is held and no ticket is issued.
          </span>
        </div>

        {priceChangeMessage && (
          <div className="notice notice--danger mb-2" role="alert">
            <AlertTriangle size={18} aria-hidden="true" />
            <div>
              <strong>The sample fare changed.</strong>
              <p className="small mb-1">{priceChangeMessage}</p>
              <button
                type="button"
                className="btn btn--lime btn--sm"
                onClick={() => {
                  const form = document.querySelector<HTMLFormElement>("#flight-booking-form");
                  if (form) form.requestSubmit();
                }}
              >
                Accept the updated fare and submit
              </button>
            </div>
          </div>
        )}

        <form
          id="flight-booking-form"
          onSubmit={handleSubmit(onSubmit)}
          noValidate
        >
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
            </label>
          </div>

          <div className="divider" />
          <h3>Passengers</h3>
          <div className="stack--sm">
            {fields.map((field, index) => (
              <div key={field.id} className="grid grid--3">
                <label className="field">
                  <span className="field__label">Passenger {index + 1}</span>
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
            <input type="checkbox" checked={accepted} onChange={(event) => setAccepted(event.target.checked)} />
            <span className="small">
              I understand this is a demo flight request. No ticket, PNR or live airline reservation will be created.
            </span>
          </label>

          <button type="submit" className="btn btn--primary btn--block mt-3" disabled={isSubmitting || !accepted}>
            {isSubmitting ? "Creating request…" : "Create demo flight request"}
          </button>
        </form>

        {offer.externalHandoffUrl && (
          <a
            className="link small row mt-2"
            href={offer.externalHandoffUrl}
            target="_blank"
            rel="noreferrer noopener"
            style={{ justifyContent: "center" }}
          >
            Search on the official airline/partner site <ExternalLink size={13} aria-hidden="true" />
          </a>
        )}
      </div>
    </div>
  );
}
