import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { BedDouble, CalendarDays, Check, Clock, Info, MapPin, X } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { ErrorState, LoadingBlock } from "../components/Feedback";
import { useAsync } from "../hooks/useAsync";
import { useToast } from "../context/ToastContext";
import { api, ApiError, newIdempotencyKey } from "../lib/api";
import { formatMoney, isoPlusDays } from "../lib/format";
import type { Booking, HolidayPackageDetail } from "../lib/types";

const bookingSchema = z.object({
  startDate: z.string().min(1, "Choose a departure date"),
  travellers: z.number({ invalid_type_error: "Enter the number of travellers" }).int().min(1).max(20),
  rooms: z.number({ invalid_type_error: "Enter the number of rooms" }).int().min(1).max(10),
  contactName: z.string().trim().min(2, "Enter a contact name").max(120),
  contactEmail: z.string().trim().email("Enter a valid email"),
  contactPhone: z.string().regex(/^$|^[0-9+\- ]{6,20}$/, "Enter a valid phone number").or(z.literal("")),
  notes: z.string().max(500).optional(),
});

type BookingValues = z.infer<typeof bookingSchema>;

export function HolidayDetailPage() {
  const { slug = "" } = useParams();
  const { data, loading, error, reload } = useAsync<HolidayPackageDetail>(
    (signal) => api.get(`/api/holidays/packages/${slug}`, signal),
    [slug],
  );

  if (loading) {
    return (
      <div className="container section">
        <LoadingBlock label="Loading the itinerary" />
      </div>
    );
  }
  if (error || !data) {
    return (
      <div className="container section">
        <ErrorState message={error ?? "That package could not be found."} onRetry={reload} />
        <Link to="/holidays" className="btn btn--ghost mt-3">
          Back to all packages
        </Link>
      </div>
    );
  }

  const pkg = data.summary;

  return (
    <div className="container">
      <PageHead
        eyebrow={`${pkg.categoryLabel} · ${pkg.destinationName}`}
        title={pkg.title}
        lede={pkg.summary}
        crumbs={[
          { to: "/", label: "Home" },
          { to: "/holidays", label: "Holiday Packages" },
          { label: pkg.destinationName },
        ]}
      />

      <div className="sidebar-layout mb-4">
        <div>
          <div className="card" style={{ overflow: "hidden" }}>
            <img
              src={pkg.imageUrl}
              alt={`Illustration for ${pkg.title}`}
              style={{ width: "100%", aspectRatio: "16/8", objectFit: "cover" }}
            />
            <div className="card--pad">
              <div className="row row--wrap">
                <span className="badge badge--lime">
                  <Clock size={12} aria-hidden="true" /> {pkg.durationDays} days / {pkg.durationNights} nights
                </span>
                <span className="badge">
                  <MapPin size={12} aria-hidden="true" /> {pkg.destinationName}, {pkg.stateOrRegion}
                </span>
                <span className="badge">
                  <BedDouble size={12} aria-hidden="true" /> {pkg.hotelsIncluded ? "Hotels included" : "Hotels not included"}
                </span>
                {pkg.landOnly && <span className="badge badge--warn">Land-only package</span>}
              </div>
              <p className="mt-2">{data.description}</p>
              <div className="honesty-strip">
                <Info size={17} aria-hidden="true" />
                <span>{data.inclusionNote}</span>
              </div>
            </div>
          </div>

          <div className="card card--pad mt-3">
            <h2>Day-by-day itinerary</h2>
            {data.itinerary.map((day) => (
              <div key={day.dayNumber} className="itinerary-day">
                <div className="itinerary-day__badge">Day {day.dayNumber}</div>
                <div>
                  <strong>{day.title}</strong>
                  <p className="small muted mb-1">{day.description}</p>
                  {day.meals && <span className="badge">Meals: {day.meals}</span>}
                </div>
              </div>
            ))}
          </div>

          <div className="grid grid--2 mt-3">
            <div className="card card--pad">
              <h3>What’s included</h3>
              <ul className="list-checks">
                {data.inclusions.map((item) => (
                  <li key={item}>
                    <Check size={15} aria-hidden="true" /> {item}
                  </li>
                ))}
              </ul>
            </div>
            <div className="card card--pad">
              <h3>Not included</h3>
              <ul className="list-checks">
                {data.exclusions.map((item) => (
                  <li key={item}>
                    <X size={15} aria-hidden="true" /> {item}
                  </li>
                ))}
              </ul>
            </div>
          </div>

          <div className="card card--pad mt-3">
            <h3>Highlights</h3>
            <div className="row row--wrap">
              {data.highlights.map((highlight) => (
                <span key={highlight} className="badge badge--lime">
                  {highlight}
                </span>
              ))}
            </div>
            <p className="small muted mt-2 mb-0">
              <strong>Cancellation policy:</strong> {data.cancellationPolicy}
            </p>
          </div>
        </div>

        <aside className="sticky-side">
          <HolidayRequestForm detail={data} />
        </aside>
      </div>
    </div>
  );
}

function HolidayRequestForm({ detail }: { detail: HolidayPackageDetail }) {
  const pkg = detail.summary;
  const navigate = useNavigate();
  const { notify } = useToast();
  const [idempotencyKey] = useState(() => newIdempotencyKey());

  const {
    register,
    watch,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<BookingValues>({
    resolver: zodResolver(bookingSchema),
    defaultValues: {
      startDate: isoPlusDays(21),
      travellers: 2,
      rooms: 1,
      contactName: "",
      contactEmail: "",
      contactPhone: "",
      notes: "",
    },
  });

  const travellers = watch("travellers") || 1;
  const total = pkg.priceMinor * travellers;

  const onSubmit = async (values: BookingValues) => {
    try {
      const booking = await api.post<Booking>("/api/bookings/holiday", {
        packageSlug: pkg.slug,
        startDate: values.startDate,
        travellers: values.travellers,
        rooms: values.rooms,
        contactName: values.contactName,
        contactEmail: values.contactEmail,
        contactPhone: values.contactPhone,
        notes: values.notes,
        idempotencyKey,
      });
      notify("Package request created. Complete the demo payment to confirm.", "ok");
      navigate(`/bookings/${booking.id}`);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 401) {
          navigate(`/login?redirect=${encodeURIComponent(`/holidays/${pkg.slug}`)}`);
          return;
        }
        notify(err.message, "error");
        return;
      }
      notify("We couldn’t create the package request.", "error");
    }
  };

  return (
    <div className="panel">
      <div className="spread">
        <div>
          <div className="tiny muted">From, per traveller</div>
          <div className="price-tag">{formatMoney(pkg.priceMinor, pkg.currency)}</div>
        </div>
        <span className="badge">{pkg.currency}</span>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-2">
        <label className="field">
          <span className="field__label">Departure date</span>
          <input type="date" className="input" min={isoPlusDays(1)} {...register("startDate")} />
          {errors.startDate && <span className="field__error">{errors.startDate.message}</span>}
        </label>
        <div className="grid grid--2">
          <label className="field">
            <span className="field__label">Travellers</span>
            <input type="number" className="input" min={1} max={20} {...register("travellers", { valueAsNumber: true })} />
            {errors.travellers && <span className="field__error">{errors.travellers.message}</span>}
          </label>
          <label className="field">
            <span className="field__label">Rooms</span>
            <input type="number" className="input" min={1} max={10} {...register("rooms", { valueAsNumber: true })} />
            {errors.rooms && <span className="field__error">{errors.rooms.message}</span>}
          </label>
        </div>
        <label className="field">
          <span className="field__label">Contact name</span>
          <input className="input" {...register("contactName")} />
          {errors.contactName && <span className="field__error">{errors.contactName.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">Email</span>
          <input type="email" className="input" {...register("contactEmail")} />
          {errors.contactEmail && <span className="field__error">{errors.contactEmail.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">Phone (optional)</span>
          <input className="input" {...register("contactPhone")} />
          {errors.contactPhone && <span className="field__error">{errors.contactPhone.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">Notes for the travel desk (optional)</span>
          <textarea className="textarea" rows={3} maxLength={500} {...register("notes")} />
        </label>

        <div className="summary-row summary-row--total">
          <span>Estimated total</span>
          <span>{formatMoney(total, pkg.currency)}</span>
        </div>
        <p className="tiny muted">
          {travellers} traveller(s) × {formatMoney(pkg.priceMinor, pkg.currency)}. Final sample price is confirmed when
          the request is created.
        </p>

        <button type="submit" className="btn btn--primary btn--block" disabled={isSubmitting}>
          {isSubmitting ? "Creating request…" : "Request this departure"}
        </button>
        <p className="tiny muted center mt-1 mb-0">
          <CalendarDays size={12} aria-hidden="true" /> Demo booking · simulated payment · no supplier reservation
        </p>
      </form>
    </div>
  );
}
