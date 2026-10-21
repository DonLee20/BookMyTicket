import { useState } from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  BedDouble,
  BusFront,
  CalendarDays,
  Compass,
  CreditCard,
  Landmark,
  MapPin,
  Plane,
  ShieldCheck,
  Sparkles,
  TrainFront,
} from "lucide-react";
import { BusSearchForm, FlightSearchForm, TrainSearchForm } from "../components/SearchForms";
import { useAsync } from "../hooks/useAsync";
import { api } from "../lib/api";
import type { HolidayHomeResponse } from "../lib/types";
import { formatMoney } from "../lib/format";

type Tab = "buses" | "trains" | "flights" | "holidays";

const tabs: { id: Tab; label: string; icon: typeof BusFront }[] = [
  { id: "buses", label: "Buses", icon: BusFront },
  { id: "trains", label: "Trains", icon: TrainFront },
  { id: "flights", label: "Flights", icon: Plane },
  { id: "holidays", label: "Holidays", icon: Compass },
];

const modes = [
  {
    icon: BusFront,
    title: "Intercity buses",
    body: "Sample operator schedules with real seat maps, boarding points and transparent demo fares.",
    to: "/buses",
  },
  {
    icon: Sparkles,
    title: "Premium coaches",
    body: "The quieter, roomier end of the road — curated sleeper and semi-sleeper layouts.",
    to: "/premium",
  },
  {
    icon: TrainFront,
    title: "Indian Railways",
    body: "Sample timetable and class availability, with a one-tap handoff to IRCTC for real tickets.",
    to: "/trains",
  },
  {
    icon: Plane,
    title: "Global flights",
    body: "Demo fares across 45 airports with revalidation before any booking request is created.",
    to: "/flights",
  },
  {
    icon: Landmark,
    title: "Chennai MTC",
    body: "Stage-by-stage route planning across 26 sample MTC routes. Ticketing stays with the official portal.",
    to: "/chennai-mtc",
  },
  {
    icon: BedDouble,
    title: "Holiday packages",
    body: "Editorial itineraries with transparent inclusions, exclusions and cancellation terms.",
    to: "/holidays",
  },
];

const honesty = [
  {
    icon: ShieldCheck,
    title: "Labelled, never guessed",
    body: "Every screen says whether data is demo, live or handed off to an official provider. There is no silent fallback.",
  },
  {
    icon: Landmark,
    title: "Rail tickets belong to IRCTC",
    body: "We prepare a booking request with your traveller details; the actual ticket is issued only on the official IRCTC portal.",
  },
  {
    icon: CreditCard,
    title: "Simulated payments, plainly said",
    body: "The demo payment step is simulated end-to-end and never touches a real card or bank account.",
  },
];

export function HomePage() {
  const [tab, setTab] = useState<Tab>("buses");
  const { data } = useAsync<HolidayHomeResponse>((signal) => api.get("/api/holidays/home", signal), []);
  const featuredDestinations = data?.featuredDestinations.slice(0, 4) ?? [];
  const featuredPackages = data?.featuredPackages.slice(0, 3) ?? [];

  return (
    <>
      <section className="hero">
        <div className="hero__media" aria-hidden="true">
          <img src="/images/hero.jpg" alt="" />
        </div>
        <div className="container hero__inner">
          <div className="eyebrow" style={{ color: "var(--lime)" }}>
            Buses · Rail · Air · City · Holidays
          </div>
          <h1 className="mt-2">
            Every journey, <em>one considered place.</em>
          </h1>
          <p className="hero__lede">
            Compare sample intercity coaches, rail timetables, flights across 45 airports, Chennai city routes and
            hand-picked holiday itineraries — with clear labels on what is demo and what is real.
          </p>
          <div className="hero__stats">
            <div className="hero__stat">
              <strong>6</strong>
              <span>travel modes, one account</span>
            </div>
            <div className="hero__stat">
              <strong>9</strong>
              <span>honest provider boundaries documented</span>
            </div>
            <div className="hero__stat">
              <strong>0</strong>
              <span>fabricated PNRs, tickets or payments</span>
            </div>
          </div>
        </div>
      </section>

      <div className="container searchbox">
        <div className="panel">
          <div className="tabs" role="tablist" aria-label="Search a travel category">
            {tabs.map(({ id, label, icon: Icon }) => (
              <button
                key={id}
                type="button"
                role="tab"
                aria-selected={tab === id}
                className={`tab${tab === id ? " active" : ""}`}
                onClick={() => setTab(id)}
              >
                <Icon size={16} aria-hidden="true" /> {label}
              </button>
            ))}
          </div>
          {tab === "buses" && <BusSearchForm />}
          {tab === "trains" && <TrainSearchForm />}
          {tab === "flights" && <FlightSearchForm />}
          {tab === "holidays" && <HolidayTeaserForm />}
        </div>
      </div>

      <section className="section">
        <div className="container">
          <div className="spread mb-3">
            <div>
              <div className="eyebrow">Ways to travel</div>
              <h2 className="mt-1 mb-0">Built around the journey you’re actually taking</h2>
            </div>
            <Link to="/explore" className="link">
              Explore all destinations
            </Link>
          </div>
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
        </div>
      </section>

      {featuredDestinations.length > 0 && (
        <section className="section section--paper">
          <div className="container">
            <div className="spread mb-3">
              <div>
                <div className="eyebrow">Featured destinations</div>
                <h2 className="mt-1 mb-0">Where the next long weekend could go</h2>
              </div>
              <Link to="/holidays" className="link">
                Browse all packages
              </Link>
            </div>
            <div className="grid grid--4">
              {featuredDestinations.map((destination) => (
                <Link key={destination.slug} to={`/holidays?destination=${destination.slug}`} className="hero-card card--hover">
                  <img src={destination.heroImageUrl} alt={`Scenery representing ${destination.name}`} loading="lazy" />
                  <div className="hero-card__body">
                    <div className="row" style={{ gap: "0.35rem" }}>
                      <MapPin size={14} aria-hidden="true" />
                      <span className="tiny">{destination.region}</span>
                    </div>
                    <h3>{destination.name}</h3>
                    <p className="tiny mb-0" style={{ color: "#d8d4c8" }}>
                      {destination.summary}
                    </p>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        </section>
      )}

      {featuredPackages.length > 0 && (
        <section className="section">
          <div className="container">
            <div className="spread mb-3">
              <div>
                <div className="eyebrow">Holiday packages</div>
                <h2 className="mt-1 mb-0">Editorial itineraries, transparent pricing</h2>
              </div>
              <Link to="/holidays" className="link">
                See every package
              </Link>
            </div>
            <div className="grid grid--3">
              {featuredPackages.map((pkg) => (
                <Link key={pkg.slug} to={`/holidays/${pkg.slug}`} className="media-card card--hover">
                  <img src={pkg.imageUrl} alt={`Illustration for ${pkg.title}`} loading="lazy" />
                  <div className="media-card__body">
                    <div className="row" style={{ gap: "0.4rem" }}>
                      <span className="badge badge--lime">{pkg.categoryLabel}</span>
                      <span className="badge">{pkg.durationDays}D / {pkg.durationNights}N</span>
                    </div>
                    <h3 className="mb-0">{pkg.title}</h3>
                    <p className="small muted mb-0">{pkg.summary}</p>
                    <div className="media-card__foot">
                      <div>
                        <div className="tiny muted">From</div>
                        <div className="price-tag">{formatMoney(pkg.priceMinor, pkg.currency)}</div>
                      </div>
                      <span className="btn btn--ghost btn--sm">View</span>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        </section>
      )}

      <section className="section section--charcoal">
        <div className="container">
          <div className="eyebrow">How we stay honest</div>
          <h2 className="mt-1 mb-2" style={{ color: "var(--cream)" }}>
            A booking desk that tells you exactly what it is
          </h2>
          <div className="grid grid--3 mt-3">
            {honesty.map(({ icon: Icon, title, body }) => (
              <div key={title} className="card card--pad" style={{ background: "#2c2c2a", borderColor: "#3c3c38" }}>
                <span className="avatar" aria-hidden="true" style={{ background: "#1d1d1b", borderColor: "#3c3c38", color: "var(--lime)" }}>
                  <Icon size={20} />
                </span>
                <h3 className="mt-2" style={{ color: "var(--cream)" }}>
                  {title}
                </h3>
                <p className="small mb-0" style={{ color: "#c6c2b7" }}>
                  {body}
                </p>
              </div>
            ))}
          </div>
          <div className="row mt-3 row--wrap">
            <Link to="/bookings" className="btn btn--lime">
              Review my bookings <ArrowRight size={16} aria-hidden="true" />
            </Link>
            <Link to="/contact" className="btn btn--ghost" style={{ borderColor: "#4a4a45", color: "var(--cream)" }}>
              Talk to the team
            </Link>
          </div>
        </div>
      </section>
    </>
  );
}

function HolidayTeaserForm() {
  const { data } = useAsync<HolidayHomeResponse>((signal) => api.get("/api/holidays/home", signal), []);
  const destinations = data?.featuredDestinations ?? [];
  return (
    <div>
      <div className="notice notice--info mb-2">
        <Compass size={18} aria-hidden="true" />
        <p className="small mb-0">
          Browse all packages or jump straight to a region. Itineraries, inclusions and prices are catalog data —
          availability is confirmed when you submit a request.
        </p>
      </div>
      <div className="row row--wrap">
        <Link to="/holidays" className="btn btn--primary">
          <CalendarDays size={17} aria-hidden="true" /> Browse holiday packages
        </Link>
        {destinations.slice(0, 5).map((destination) => (
          <Link key={destination.slug} to={`/holidays?destination=${destination.slug}`} className="chip chip--static">
            {destination.name}
          </Link>
        ))}
      </div>
    </div>
  );
}
