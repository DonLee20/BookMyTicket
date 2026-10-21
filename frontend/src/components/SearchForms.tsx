import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useNavigate } from "react-router-dom";
import { Search } from "lucide-react";
import { api } from "../lib/api";
import { useAsync } from "../hooks/useAsync";
import { isoPlusDays, todayIso } from "../lib/format";
import type { AirportOption, StationOption } from "../lib/types";

interface CitiesResponse {
  cities: string[];
  origin: boolean;
}

const busSchema = z
  .object({
    from: z.string().min(1, "Choose a boarding city"),
    to: z.string().min(1, "Choose a destination"),
    date: z.string().min(1, "Pick a travel date"),
    passengers: z.number({ invalid_type_error: "Enter the number of travellers" }).int().min(1, "At least one traveller").max(6, "Maximum six travellers"),
  })
  .refine((value) => value.from !== value.to, {
    message: "Boarding and destination cities must differ",
    path: ["to"],
  });

const trainSchema = busSchema;

const flightSchema = z
  .object({
    origin: z.string().min(1, "Choose a departure airport"),
    destination: z.string().min(1, "Choose an arrival airport"),
    date: z.string().min(1, "Pick a travel date"),
    cabin: z.string().min(1),
    adults: z.number({ invalid_type_error: "Enter the number of adults" }).int().min(1).max(9),
  })
  .refine((value) => value.origin !== value.destination, {
    message: "Departure and arrival airports must differ",
    path: ["destination"],
  });

type BusFormValues = z.infer<typeof busSchema>;
type FlightFormValues = z.infer<typeof flightSchema>;

export interface BusSearchDefaults {
  from?: string;
  to?: string;
  date?: string;
  passengers?: number;
}

export function BusSearchForm({
  defaults,
  premiumOnly = false,
  basePath = "/buses",
}: {
  defaults?: BusSearchDefaults;
  premiumOnly?: boolean;
  basePath?: string;
}) {
  const navigate = useNavigate();
  const { data } = useAsync<CitiesResponse>((signal) => api.get("/api/buses/cities", signal), []);
  const cities = data?.cities ?? [];

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<BusFormValues>({
    resolver: zodResolver(busSchema),
    defaultValues: {
      from: defaults?.from ?? "Chennai",
      to: defaults?.to ?? cities.find((city) => city !== (defaults?.from ?? "Chennai")) ?? "",
      date: defaults?.date ?? isoPlusDays(1),
      passengers: defaults?.passengers ?? 1,
    },
  });

  const onSubmit = (values: BusFormValues) => {
    const params = new URLSearchParams({
      from: values.from,
      to: values.to,
      date: values.date,
      passengers: String(values.passengers),
    });
    if (premiumOnly) params.set("premium", "true");
    navigate(`${basePath}?${params.toString()}`);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate>
      <div className="search-grid">
        <label className="field">
          <span className="field__label">From</span>
          <select className="select" aria-invalid={Boolean(errors.from)} {...register("from")}>
            <option value="">Select city</option>
            {cities.map((city) => (
              <option key={city} value={city}>
                {city}
              </option>
            ))}
          </select>
          {errors.from && <span className="field__error">{errors.from.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">To</span>
          <select className="select" aria-invalid={Boolean(errors.to)} {...register("to")}>
            <option value="">Select city</option>
            {cities.map((city) => (
              <option key={city} value={city}>
                {city}
              </option>
            ))}
          </select>
          {errors.to && <span className="field__error">{errors.to.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">Travel date</span>
          <input
            type="date"
            className="input"
            min={todayIso()}
            aria-invalid={Boolean(errors.date)}
            {...register("date")}
          />
          {errors.date && <span className="field__error">{errors.date.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">Travellers</span>
          <input type="number" className="input" min={1} max={6} {...register("passengers", { valueAsNumber: true })} />
          {errors.passengers && <span className="field__error">{errors.passengers.message}</span>}
        </label>
        <button type="submit" className="btn btn--primary" disabled={isSubmitting}>
          <Search size={17} aria-hidden="true" /> Search buses
        </button>
      </div>
    </form>
  );
}

export interface TrainSearchDefaults {
  from?: string;
  to?: string;
  date?: string;
  passengers?: number;
}

export function TrainSearchForm({ defaults }: { defaults?: TrainSearchDefaults }) {
  const navigate = useNavigate();
  const { data } = useAsync<StationOption[]>((signal) => api.get("/api/trains/stations", signal), []);
  const stations = useMemo(
    () => [...(data ?? [])].sort((a, b) => a.city.localeCompare(b.city) || a.name.localeCompare(b.name)),
    [data],
  );

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<BusFormValues>({
    resolver: zodResolver(trainSchema),
    defaultValues: {
      from: defaults?.from ?? "MAS",
      to: defaults?.to ?? "CBE",
      date: defaults?.date ?? isoPlusDays(1),
      passengers: defaults?.passengers ?? 1,
    },
  });

  const onSubmit = (values: BusFormValues) => {
    const params = new URLSearchParams({
      from: values.from,
      to: values.to,
      date: values.date,
      passengers: String(values.passengers),
    });
    navigate(`/trains?${params.toString()}`);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate>
      <div className="search-grid">
        <label className="field">
          <span className="field__label">From station</span>
          <select className="select" aria-invalid={Boolean(errors.from)} {...register("from")}>
            <option value="">Select station</option>
            {stations.map((station) => (
              <option key={station.code} value={station.code}>
                {station.city} — {station.name} ({station.code})
              </option>
            ))}
          </select>
          {errors.from && <span className="field__error">{errors.from.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">To station</span>
          <select className="select" aria-invalid={Boolean(errors.to)} {...register("to")}>
            <option value="">Select station</option>
            {stations.map((station) => (
              <option key={station.code} value={station.code}>
                {station.city} — {station.name} ({station.code})
              </option>
            ))}
          </select>
          {errors.to && <span className="field__error">{errors.to.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">Journey date</span>
          <input type="date" className="input" min={todayIso()} {...register("date")} />
          {errors.date && <span className="field__error">{errors.date.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">Passengers</span>
          <input type="number" className="input" min={1} max={6} {...register("passengers", { valueAsNumber: true })} />
          {errors.passengers && <span className="field__error">{errors.passengers.message}</span>}
        </label>
        <button type="submit" className="btn btn--primary" disabled={isSubmitting}>
          <Search size={17} aria-hidden="true" /> Search trains
        </button>
      </div>
    </form>
  );
}

const cabinOptions = [
  { code: "ECONOMY", label: "Economy" },
  { code: "PREMIUM_ECONOMY", label: "Premium economy" },
  { code: "BUSINESS", label: "Business" },
  { code: "FIRST", label: "First" },
];

export interface FlightSearchDefaults {
  origin?: string;
  destination?: string;
  date?: string;
  adults?: number;
  cabin?: string;
}

export function FlightSearchForm({ defaults }: { defaults?: FlightSearchDefaults }) {
  const navigate = useNavigate();
  const { data } = useAsync<AirportOption[]>((signal) => api.get("/api/flights/airports", signal), []);
  const airports = useMemo(
    () => [...(data ?? [])].sort((a, b) => a.city.localeCompare(b.city)),
    [data],
  );

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FlightFormValues>({
    resolver: zodResolver(flightSchema),
    defaultValues: {
      origin: defaults?.origin ?? "MAA",
      destination: defaults?.destination ?? "BLR",
      date: defaults?.date ?? isoPlusDays(3),
      cabin: defaults?.cabin ?? "ECONOMY",
      adults: defaults?.adults ?? 1,
    },
  });

  const onSubmit = (values: FlightFormValues) => {
    const params = new URLSearchParams({
      origin: values.origin,
      destination: values.destination,
      date: values.date,
      cabin: values.cabin,
      adults: String(values.adults),
    });
    navigate(`/flights?${params.toString()}`);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate>
      <div className="search-grid search-grid--3">
        <label className="field">
          <span className="field__label">From</span>
          <select className="select" aria-invalid={Boolean(errors.origin)} {...register("origin")}>
            <option value="">Select airport</option>
            {airports.map((airport) => (
              <option key={airport.iata} value={airport.iata}>
                {airport.city} ({airport.iata})
              </option>
            ))}
          </select>
          {errors.origin && <span className="field__error">{errors.origin.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">To</span>
          <select className="select" aria-invalid={Boolean(errors.destination)} {...register("destination")}>
            <option value="">Select airport</option>
            {airports.map((airport) => (
              <option key={airport.iata} value={airport.iata}>
                {airport.city} ({airport.iata})
              </option>
            ))}
          </select>
          {errors.destination && <span className="field__error">{errors.destination.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">Departure date</span>
          <input type="date" className="input" min={todayIso()} {...register("date")} />
          {errors.date && <span className="field__error">{errors.date.message}</span>}
        </label>
        <label className="field">
          <span className="field__label">Cabin</span>
          <select className="select" {...register("cabin")}>
            {cabinOptions.map((cabin) => (
              <option key={cabin.code} value={cabin.code}>
                {cabin.label}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          <span className="field__label">Adults</span>
          <input type="number" className="input" min={1} max={9} {...register("adults", { valueAsNumber: true })} />
        </label>
        <button type="submit" className="btn btn--primary" disabled={isSubmitting}>
          <Search size={17} aria-hidden="true" /> Search flights
        </button>
      </div>
    </form>
  );
}
