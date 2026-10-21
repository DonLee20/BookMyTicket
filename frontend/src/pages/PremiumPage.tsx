import { useMemo } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Armchair, Coffee, Moon, Sparkles, Wifi } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { BusSearchForm } from "../components/SearchForms";
import { EmptyState, ErrorState, LoadingBlock, ProviderNotice } from "../components/Feedback";
import { TripCard } from "../components/TripCard";
import { useAsync } from "../hooks/useAsync";
import { api } from "../lib/api";
import { formatDateLong } from "../lib/format";
import type { BusSearchResponse, PremiumHighlights } from "../lib/types";

const comfort = [
  { icon: Moon, title: "Sleeper-first layouts", body: "2×1 sleeper coaches with privacy curtains on the long overnight runs." },
  { icon: Armchair, title: "Wider seat pitch", body: "Semi-sleeper and recliner layouts where legroom matters more than seat count." },
  { icon: Wifi, title: "Working on the move", body: "Charging points and reliable stop patterns for laptop-friendly journeys." },
  { icon: Coffee, title: "Considered boarding", body: "Fewer, better-connected boarding points so the first mile is predictable." },
];

export function PremiumPage() {
  const [params] = useSearchParams();
  const from = params.get("from") ?? "";
  const to = params.get("to") ?? "";
  const date = params.get("date") ?? "";
  const passengers = Number(params.get("passengers") ?? 1);
  const hasSearch = Boolean(from && to && date);

  const highlights = useAsync<PremiumHighlights>((signal) => api.get("/api/premium/highlights", signal), []);

  const { data, loading, error, reload } = useAsync<BusSearchResponse | null>(
    (signal) =>
      hasSearch
        ? api.post("/api/premium/search", { from, to, date, passengers, premiumOnly: true }, signal)
        : Promise.resolve(null),
    [from, to, date, passengers, hasSearch],
  );

  const trips = useMemo(() => {
    if (data) return data.trips;
    return highlights.data?.trips ?? [];
  }, [data, highlights.data]);

  const providerMode = data?.providerMode ?? highlights.data?.providerMode;
  const providerMessage = data?.providerMessage ?? highlights.data?.providerMessage;
  const liveDataConnected = data?.liveDataConnected ?? highlights.data?.liveDataConnected;
  const liveProviderConfigured = data?.liveProviderConfigured ?? highlights.data?.liveProviderConfigured;

  return (
    <div className="container">
      <PageHead
        eyebrow="Premium travel"
        title="The roomier end of the road"
        lede="Premium coaches from the demo inventory: sleeper-first layouts, wider seats and quieter boarding points. Same honest labelling — sample schedules, real seat maps, clearly simulated payment."
        crumbs={[{ to: "/", label: "Home" }, { label: "Premium Travel" }]}
      />

      {providerMessage && (
        <ProviderNotice
          mode={providerMode}
          message={providerMessage}
          liveDataConnected={liveDataConnected}
          liveProviderConfigured={liveProviderConfigured}
        />
      )}

      <div className="grid grid--4 mt-3 mb-4">
        {comfort.map(({ icon: Icon, title, body }) => (
          <div key={title} className="card card--pad">
            <span className="avatar" aria-hidden="true">
              <Icon size={19} />
            </span>
            <h3 className="mt-2">{title}</h3>
            <p className="small muted mb-0">{body}</p>
          </div>
        ))}
      </div>

      <div className="panel mb-3">
        <div className="row mb-2">
          <Sparkles size={18} aria-hidden="true" />
          <strong>Search premium coaches only</strong>
        </div>
        <BusSearchForm
          premiumOnly
          basePath="/premium"
          defaults={{
            from: from || undefined,
            to: to || undefined,
            date: date || undefined,
            passengers: Number.isFinite(passengers) ? passengers : undefined,
          }}
        />
      </div>

      <div className="spread mb-2">
        <h2 className="mb-0">{hasSearch ? `${from} to ${to}` : "A sample of premium coaches"}</h2>
        {hasSearch && (
          <p className="small muted mb-0">
            {formatDateLong(date)} · {data ? `${data.trips.length} premium trips` : "loading"}
          </p>
        )}
      </div>

      {loading || highlights.loading ? (
        <LoadingBlock label="Finding premium coaches" />
      ) : error ? (
        <ErrorState message={error} onRetry={reload} />
      ) : trips.length === 0 ? (
        <EmptyState title="No premium coaches on this route">
          <p>
            Premium inventory is a subset of the demo dataset. Try a major corridor or an evening departure, or browse
            all coaches.
          </p>
          <Link to="/buses" className="btn btn--ghost btn--sm mt-2">
            Search all buses
          </Link>
        </EmptyState>
      ) : (
        <section className="mb-4">
          {trips.map((trip) => (
            <TripCard key={trip.id} trip={trip} premium />
          ))}
        </section>
      )}
    </div>
  );
}
