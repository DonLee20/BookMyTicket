import { useState } from "react";
import { Accessibility, ArrowLeftRight, Bus, ExternalLink, Info, Landmark, Search } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { EmptyState, ErrorState, LoadingBlock, ProviderNotice } from "../components/Feedback";
import { useAsync } from "../hooks/useAsync";
import { api } from "../lib/api";
import type { MtcPlanResponse, MtcRoutePlan, MtcStop } from "../lib/types";

export function MtcPage() {
  const [originId, setOriginId] = useState<string>("");
  const [destinationId, setDestinationId] = useState<string>("");
  const [plan, setPlan] = useState<MtcPlanResponse | null>(null);
  const [planLoading, setPlanLoading] = useState(false);
  const [planError, setPlanError] = useState<string | null>(null);

  const { data: stops, loading, error, reload } = useAsync<MtcStop[]>((signal) => api.get("/api/mtc/stops", signal), []);
  const sortedStops = [...(stops ?? [])].sort((a, b) => a.name.localeCompare(b.name));

  const submit = async () => {
    if (!originId || !destinationId) return;
    setPlanLoading(true);
    setPlanError(null);
    try {
      const response = await api.post<MtcPlanResponse>("/api/mtc/plan", {
        originStopId: Number(originId),
        destinationStopId: Number(destinationId),
      });
      setPlan(response);
    } catch (err) {
      setPlanError(err instanceof Error ? err.message : "We couldn’t plan this trip.");
      setPlan(null);
    } finally {
      setPlanLoading(false);
    }
  };

  const swap = () => {
    setOriginId(destinationId);
    setDestinationId(originId);
    setPlan(null);
  };

  return (
    <div className="container">
      <PageHead
        eyebrow="Chennai MTC · sample route information"
        title="Plan a city stage, not a ticket"
        lede="Stage-by-stage route planning across 26 sample MTC routes and 31 stops. Route shapes, frequencies and fares are sample data for planning only — official ticketing stays with the MTC portal."
        crumbs={[{ to: "/", label: "Home" }, { label: "Chennai MTC" }]}
      />

      <div className="grid grid--2 mb-3">
        <div className="panel">
          <h2>Plan your trip</h2>
          <div className="grid grid--2">
            <label className="field">
              <span className="field__label">From stop</span>
              <select className="select" value={originId} onChange={(event) => setOriginId(event.target.value)}>
                <option value="">Select origin</option>
                {sortedStops.map((stop) => (
                  <option key={stop.id} value={stop.id}>
                    {stop.name} · {stop.locality}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              <span className="field__label">To stop</span>
              <select className="select" value={destinationId} onChange={(event) => setDestinationId(event.target.value)}>
                <option value="">Select destination</option>
                {sortedStops.map((stop) => (
                  <option key={stop.id} value={stop.id}>
                    {stop.name} · {stop.locality}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <div className="row mt-2">
            <button type="button" className="btn btn--ghost btn--sm" onClick={swap} aria-label="Swap origin and destination">
              <ArrowLeftRight size={15} aria-hidden="true" /> Swap
            </button>
            <button
              type="button"
              className="btn btn--primary"
              onClick={submit}
              disabled={!originId || !destinationId || originId === destinationId || planLoading}
            >
              <Search size={16} aria-hidden="true" /> {planLoading ? "Planning…" : "Find routes"}
            </button>
          </div>
          {originId && originId === destinationId && (
            <p className="field__error">Choose two different stops to plan a route.</p>
          )}
        </div>

        <div className="panel">
          <span className="avatar" aria-hidden="true">
            <Landmark size={20} />
          </span>
          <h2 className="mt-2">Ticketing is not offered here</h2>
          <p className="small muted">
            This platform does not sell MTC tickets or passes, and does not create a digital journey ticket. Stage
            fares shown are sample reference values. For real tickets and live service updates, use the official MTC
            portal.
          </p>
          <a className="link small" href="https://www.mtcbus.tn.gov.in/" target="_blank" rel="noreferrer noopener">
            Official MTC website <ExternalLink size={13} aria-hidden="true" />
          </a>
        </div>
      </div>

      {loading && <LoadingBlock label="Loading sample stops" />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && planError && <ErrorState message={planError} />}

      {plan && (
        <section aria-label="Route plans" className="mb-4">
          <ProviderNotice mode={plan.providerMode} message={plan.message} sourceTimestamp={plan.sourceTimestamp} />
          <div className="spread mt-2 mb-2">
            <h2 className="mb-0">{plan.routes.length > 0 ? `${plan.routes.length} sample route plan(s)` : "No direct sample route"}</h2>
            <span className="badge">{plan.dataFreshness}</span>
          </div>
          {plan.transferSuggestion && <p className="small muted">{plan.transferSuggestion}</p>}

          {plan.routes.length === 0 && (
            <EmptyState title="No direct sample route found" icon={<Bus size={30} aria-hidden="true" />}>
              <p>Try a nearby stop pair within the same zone. This demo network covers 26 routes across the city.</p>
            </EmptyState>
          )}

          {plan.routes.map((route) => (
            <RouteCard key={`${route.routeId}-${route.direction}`} route={route} />
          ))}

          <div className="notice notice--warn mt-3">
            <Info size={18} aria-hidden="true" />
            <div>
              <strong>Sample information — ticketing unavailable</strong>
              <p className="small mb-1">{plan.ticketing.message}</p>
              <a href={plan.ticketing.officialUrl} target="_blank" rel="noreferrer noopener" className="link small">
                {plan.ticketing.officialUrl} <ExternalLink size={13} aria-hidden="true" />
              </a>
            </div>
          </div>
        </section>
      )}
    </div>
  );
}

function RouteCard({ route }: { route: MtcRoutePlan }) {
  const [expanded, setExpanded] = useState(true);
  return (
    <article className="card card--pad mt-2">
      <div className="spread">
        <div className="row">
          <span className="avatar" aria-hidden="true">
            {route.routeNumber.replace(/[^0-9A-Za-z]/g, "").slice(0, 3)}
          </span>
          <div>
            <strong>
              Route {route.routeNumber} · {route.directionLabel}
            </strong>
            <div className="tiny muted">
              {route.originName} → {route.destinationName} · {route.travelStages} stages
            </div>
          </div>
        </div>
        <div className="row row--wrap">
          {route.accessible && (
            <span className="badge badge--info">
              <Accessibility size={13} aria-hidden="true" /> Accessible
            </span>
          )}
          <span className="badge">{route.serviceDays}</span>
          <span className="badge badge--lime">{route.segmentFareDisplay} sample fare</span>
        </div>
      </div>
      <div className="dl-inline tiny muted mt-2">
        <span>First service {route.firstService}</span>
        <span>Last service {route.lastService}</span>
        {route.frequencyMinutes && <span>Every ~{route.frequencyMinutes} min</span>}
        <span>{route.dataNote}</span>
      </div>

      <button type="button" className="btn btn--ghost btn--sm mt-2" onClick={() => setExpanded((value) => !value)}>
        {expanded ? "Hide" : "Show"} stop-by-stop view
      </button>
      {expanded && (
        <ol className="mt-2" style={{ display: "grid", gap: "0.45rem" }}>
          {route.stops.map((stop) => (
            <li
              key={`${stop.stopId}-${stop.sequence}`}
              className="row small"
              style={{
                justifyContent: "space-between",
                padding: "0.35rem 0.6rem",
                borderRadius: 8,
                background: stop.origin || stop.destination ? "var(--lime-soft)" : "transparent",
                border: stop.origin || stop.destination ? "1px solid #cdf29b" : "1px dashed var(--line)",
              }}
            >
              <span>
                <strong>{stop.sequence + 1}.</strong> {stop.name}{" "}
                {stop.origin && <span className="badge badge--dark">Board</span>}
                {stop.destination && <span className="badge badge--dark">Alight</span>}
              </span>
              <span className="muted">
                Stage {stop.stageNumber} · {stop.fareFromOriginDisplay} · {stop.distanceKm} km
              </span>
            </li>
          ))}
        </ol>
      )}
    </article>
  );
}
