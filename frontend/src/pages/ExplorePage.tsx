import { Link } from "react-router-dom";
import { ArrowRight, BusFront, Compass, Landmark, MapPin, Plane, Sparkles, TrainFront } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { ErrorState, LoadingBlock } from "../components/Feedback";
import { useAsync } from "../hooks/useAsync";
import { api } from "../lib/api";
import type { HolidaySearchResponse } from "../lib/types";

const modes = [
  { icon: BusFront, title: "Buses", body: "Sample intercity schedules with real seat-map layouts.", to: "/buses" },
  { icon: Sparkles, title: "Premium Travel", body: "Sleeper-first coaches for longer, quieter journeys.", to: "/premium" },
  { icon: TrainFront, title: "Trains", body: "Sample Indian Railways timetables with IRCTC handoff.", to: "/trains" },
  { icon: Plane, title: "Flights", body: "Demo fares across 45 airports worldwide.", to: "/flights" },
  { icon: Landmark, title: "Chennai MTC", body: "Stage-by-stage city route planning, no ticketing.", to: "/chennai-mtc" },
  { icon: Compass, title: "Holiday Packages", body: "Editorial itineraries with transparent inclusions.", to: "/holidays" },
];

export function ExplorePage() {
  const { data, loading, error, reload } = useAsync<HolidaySearchResponse>(
    (signal) => api.get("/api/holidays/packages", signal),
    [],
  );

  return (
    <div className="container">
      <PageHead
        eyebrow="Explore"
        title="Start with a place, then choose how to get there"
        lede="Destinations from the sample holiday catalogue, paired with every transport mode on the platform. Package details are illustrative; schedules and fares are clearly labelled demo data."
        crumbs={[{ to: "/", label: "Home" }, { label: "Explore" }]}
      />

      <section aria-label="Travel modes" className="mb-4">
        <div className="grid grid--3">
          {modes.map(({ icon: Icon, title, body, to }) => (
            <Link key={title} to={to} className="card card--pad card--hover">
              <span className="avatar" aria-hidden="true">
                <Icon size={20} />
              </span>
              <h3 className="mt-2">{title}</h3>
              <p className="small muted mb-0">{body}</p>
              <span className="link small mt-2">
                Open <ArrowRight size={14} aria-hidden="true" />
              </span>
            </Link>
          ))}
        </div>
      </section>

      <div className="spread mb-2">
        <h2 className="mb-0">Destinations in the sample catalogue</h2>
        {data && <span className="badge badge--warn">Demo catalogue</span>}
      </div>

      {loading && <LoadingBlock label="Loading destinations" />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}

      <div className="grid grid--3 mb-4">
        {!loading &&
          data?.destinations.map((destination) => (
            <Link key={destination.slug} to={`/holidays?destination=${destination.slug}`} className="hero-card card--hover">
              <img src={destination.heroImageUrl} alt={`Scenery representing ${destination.name}`} loading="lazy" />
              <div className="hero-card__body">
                <div className="row tiny" style={{ gap: "0.35rem" }}>
                  <MapPin size={13} aria-hidden="true" /> {destination.region}
                </div>
                <h3>{destination.name}</h3>
                <p className="tiny mb-0" style={{ color: "#d8d4c8" }}>
                  {destination.summary}
                </p>
                <div className="row tiny mt-1" style={{ gap: "0.4rem", color: "#d8d4c8" }}>
                  <span className="badge badge--dark">{destination.kind === "INTERNATIONAL" ? "International" : "India"}</span>
                  <span>Best season: {destination.bestSeason}</span>
                </div>
              </div>
            </Link>
          ))}
      </div>
    </div>
  );
}
