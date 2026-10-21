import { useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { BusFront, CalendarX2, SlidersHorizontal } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { BusSearchForm } from "../components/SearchForms";
import { EmptyState, ErrorState, ProviderNotice } from "../components/Feedback";
import { TripCard } from "../components/TripCard";
import { useAsync } from "../hooks/useAsync";
import { api } from "../lib/api";
import { formatDateLong } from "../lib/format";
import type { BusSearchDiagnostics, BusSearchResponse, BusTrip } from "../lib/types";

type SortKey = "departure" | "fare" | "duration";

export function BusPage() {
  const [params] = useSearchParams();
  const from = params.get("from") ?? "";
  const to = params.get("to") ?? "";
  const date = params.get("date") ?? "";
  const passengers = Number(params.get("passengers") ?? 1);
  const hasSearch = Boolean(from && to && date);

  return (
    <div className="container">
      <PageHead
        eyebrow="Intercity buses"
        title={hasSearch ? "Choose your coach" : "The road, without the guesswork"}
        lede={
          hasSearch
            ? "Compare departures, coach types, boarding points and fares. The provider notice below tells you whether these results are live or clearly labelled demo data."
            : "Search sample intercity schedules for any route and date. Every seat map is generated from the actual coach layout."
        }
        crumbs={[{ to: "/", label: "Home" }, { label: "Buses" }]}
      />

      <div className="panel mb-3">
        <BusSearchForm
          defaults={{
            from: from || undefined,
            to: to || undefined,
            date: date || undefined,
            passengers: Number.isFinite(passengers) ? passengers : undefined,
          }}
        />
      </div>

      {hasSearch ? (
        <BusResults from={from} to={to} date={date} passengers={passengers} />
      ) : (
        <div className="grid grid--2 mb-4">
          <div className="card card--pad">
            <span className="avatar" aria-hidden="true">
              <BusFront size={20} />
            </span>
            <h2 className="mt-2">What you can do here</h2>
            <p className="muted small">
              Pick a route and date to see sample trips. Compare departures, coach type, amenities and live (sample)
              seat availability. Select seats and continue to a demo checkout.
            </p>
            <p className="small mb-0">
              Need the roomier option? <Link to="/premium" className="link">Browse premium coaches</Link>.
            </p>
          </div>
          <div className="card card--pad">
            <span className="avatar" aria-hidden="true">
              <SlidersHorizontal size={20} />
            </span>
            <h2 className="mt-2">Popular demo corridors</h2>
            <div className="row row--wrap">
              {[
                ["Chennai", "Coimbatore"],
                ["Bengaluru", "Mysuru"],
                ["Chennai", "Puducherry"],
                ["Coimbatore", "Ooty"],
                ["Chennai", "Tirupati"],
                ["Hyderabad", "Vijayawada"],
              ].map(([origin, destination]) => (
                <Link
                  key={`${origin}-${destination}`}
                  to={`/buses?from=${origin}&to=${destination}&date=${date || new Date().toISOString().slice(0, 10)}&passengers=1`}
                  className="chip"
                >
                  {origin} → {destination}
                </Link>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function BusResults({ from, to, date, passengers }: { from: string; to: string; date: string; passengers: number }) {
  const [sort, setSort] = useState<SortKey>("departure");
  const [typeFilter, setTypeFilter] = useState<string>("ALL");

  const { data, loading, error, reload } = useAsync<BusSearchResponse>(
    (signal) =>
      api.post(
        "/api/buses/search",
        { from, to, date, passengers: Number.isFinite(passengers) ? passengers : 1 },
        signal,
      ),
    [from, to, date, passengers],
  );

  const isLive = data?.providerMode === "LIVE";

  const types = useMemo(() => {
    const unique = new Map<string, string>();
    data?.trips.forEach((trip) => unique.set(trip.busType, trip.busTypeLabel));
    return [...unique.entries()];
  }, [data]);

  const visible = useMemo(() => {
    const trips = (data?.trips ?? []).filter((trip) => typeFilter === "ALL" || trip.busType === typeFilter);
    return [...trips].sort((a, b) => {
      if (sort === "fare") return a.fareMinor - b.fareMinor;
      if (sort === "duration") return a.durationMinutes - b.durationMinutes;
      return a.departTime.localeCompare(b.departTime);
    });
  }, [data, sort, typeFilter]);

  return (
    <section aria-label="Bus search results" className="mb-4">
      <div className="spread mb-2">
        <div>
          <h2 className="mb-0">
            {from} to {to}
          </h2>
          <p className="small muted mb-0">
            {formatDateLong(date)} · {passengers} {passengers === 1 ? "traveller" : "travellers"}
            {data ? ` · ${data.trips.length} ${isLive ? "live" : "sample"} trips` : ""}
          </p>
        </div>
        <div className="row row--wrap">
          <label className="row small" style={{ gap: "0.4rem" }}>
            <span className="muted">Sort</span>
            <select className="select" style={{ width: "auto" }} value={sort} onChange={(event) => setSort(event.target.value as SortKey)}>
              <option value="departure">Departure</option>
              <option value="fare">Lowest fare</option>
              <option value="duration">Shortest journey</option>
            </select>
          </label>
        </div>
      </div>

      {data && (
        <ProviderNotice
          mode={data.providerMode}
          message={data.providerMessage}
          sourceTimestamp={data.sourceTimestamp}
          liveDataConnected={data.liveDataConnected}
          liveProviderConfigured={data.liveProviderConfigured}
        />
      )}

      {types.length > 1 && (
        <div className="pill-group mt-2 mb-2" aria-label="Filter by coach type">
          <button type="button" className={`chip${typeFilter === "ALL" ? " active" : ""}`} onClick={() => setTypeFilter("ALL")}>
            All coaches
          </button>
          {types.map(([code, label]) => (
            <button
              key={code}
              type="button"
              className={`chip${typeFilter === code ? " active" : ""}`}
              onClick={() => setTypeFilter(code)}
            >
              {label}
            </button>
          ))}
        </div>
      )}

      <div className="mt-2">
        {loading && <BusSkeletons />}
        {!loading && error && <ErrorState message={error} onRetry={reload} />}
        {!loading && !error && data && visible.length === 0 && data.trips.length > 0 && (
          <EmptyState title="No coaches match this filter" icon={<BusFront size={30} aria-hidden="true" />}>
            <p>
              This route has {data.trips.length} {isLive ? "live" : "sample"} trips, but none match the selected coach
              type.
            </p>
            <button type="button" className="btn btn--ghost btn--sm mt-2" onClick={() => setTypeFilter("ALL")}>
              Show all coaches
            </button>
          </EmptyState>
        )}
        {!loading && !error && data && visible.length === 0 && data.trips.length === 0 && (
          <BusEmptyState
            diagnostics={data.diagnostics}
            from={from}
            to={to}
            date={date}
            passengers={passengers}
          />
        )}
        {!loading && !error && visible.map((trip: BusTrip) => <TripCard key={trip.id} trip={trip} />)}
      </div>
    </section>
  );
}

function BusEmptyState({
  diagnostics,
  from,
  to,
  date,
  passengers,
}: {
  diagnostics?: BusSearchDiagnostics;
  from: string;
  to: string;
  date: string;
  passengers: number;
}) {
  const status = diagnostics?.status;
  const suggestions = diagnostics?.suggestions ?? [];
  const routeIssue =
    status === "ROUTE_NOT_COVERED" ||
    status === "ORIGIN_NOT_COVERED" ||
    status === "DESTINATION_NOT_COVERED";
  const dateIssue = status === "OUTSIDE_DEMO_WINDOW" || status === "NO_TRIPS_ON_DATE";
  const title = (() => {
    switch (status) {
      case "ORIGIN_NOT_COVERED":
        return "Boarding city not in the demo network";
      case "DESTINATION_NOT_COVERED":
        return "Destination not in the demo network";
      case "ROUTE_NOT_COVERED":
        return "No demo route for this pair";
      case "OUTSIDE_DEMO_WINDOW":
        return "Outside the demo booking window";
      case "NO_TRIPS_ON_DATE":
        return "No sample coaches on this date";
      case "FILTERED_OUT":
        return "No coaches match these filters";
      default:
        return "No coaches found";
    }
  })();

  return (
    <EmptyState
      title={title}
      icon={
        dateIssue ? (
          <CalendarX2 size={30} aria-hidden="true" />
        ) : (
          <BusFront size={30} aria-hidden="true" />
        )
      }
    >
      <p>{diagnostics?.message ?? `No coaches are available from ${from} to ${to}.`}</p>
      {suggestions.length > 0 && (
        <div className="mt-2">
          <div className="tiny muted mb-1">Demo routes you can try</div>
          <div className="row row--wrap" style={{ justifyContent: "center" }}>
            {suggestions.map((route) => {
              const query = new URLSearchParams({
                from: route.from,
                to: route.to,
                date,
                passengers: String(passengers),
              }).toString();
              return (
                <Link key={`${route.from}-${route.to}`} to={`/buses?${query}`} className="chip">
                  {route.from} → {route.to}
                </Link>
              );
            })}
          </div>
        </div>
      )}
      <div className="row mt-2" style={{ justifyContent: "center" }}>
        <Link to="/buses" className="btn btn--ghost btn--sm">
          Browse demo routes
        </Link>
        {!routeIssue && (
          <Link to="/premium" className="btn btn--primary btn--sm">
            Premium coaches
          </Link>
        )}
      </div>
    </EmptyState>
  );
}

function BusSkeletons() {
  return (
    <div className="stack" aria-hidden="true">
      {[0, 1, 2].map((index) => (
        <div key={index} className="skeleton" style={{ height: 168 }} />
      ))}
    </div>
  );
}
