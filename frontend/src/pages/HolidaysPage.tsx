import { useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Compass, Filter, MapPin, Search } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { EmptyState, ErrorState, LoadingBlock } from "../components/Feedback";
import { useAsync } from "../hooks/useAsync";
import { api } from "../lib/api";
import { formatMoney, humanise } from "../lib/format";
import type { HolidaySearchResponse } from "../lib/types";

export function HolidaysPage() {
  const [params, setParams] = useSearchParams();
  const destination = params.get("destination") ?? "";
  const category = params.get("category") ?? "";
  const kind = params.get("kind") ?? "";
  const q = params.get("q") ?? "";
  const [query, setQuery] = useState(q);

  const url = useMemo(() => {
    const search = new URLSearchParams();
    if (destination) search.set("destination", destination);
    if (category) search.set("category", category);
    if (kind) search.set("kind", kind);
    if (q) search.set("q", q);
    const suffix = search.toString();
    return `/api/holidays/packages${suffix ? `?${suffix}` : ""}`;
  }, [destination, category, kind, q]);

  const { data, loading, error, reload } = useAsync<HolidaySearchResponse>(
    (signal) => api.get(url, signal),
    [url],
  );

  const update = (key: string, value: string) => {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    setParams(next, { replace: true });
  };

  const filtered = data?.packages ?? [];

  return (
    <div className="container">
      <PageHead
        eyebrow="Holiday packages"
        title="Itineraries written like travel, not like inventory"
        lede="Fourteen sample packages across India and beyond, each with a day-by-day plan, transparent inclusions and a cancellation policy. Prices are catalog reference prices confirmed when you submit a request."
        crumbs={[{ to: "/", label: "Home" }, { label: "Holiday Packages" }]}
      />

      <div className="panel mb-3">
        <div className="search-grid search-grid--3">
          <label className="field">
            <span className="field__label">Keyword</span>
            <input
              className="input"
              placeholder="Tea estates, backwaters, temple towns…"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") update("q", query);
              }}
            />
          </label>
          <label className="field">
            <span className="field__label">Destination</span>
            <select className="select" value={destination} onChange={(event) => update("destination", event.target.value)}>
              <option value="">All destinations</option>
              {(data?.destinations ?? []).map((item) => (
                <option key={item.slug} value={item.slug}>
                  {item.name}, {item.region}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span className="field__label">Category</span>
            <select className="select" value={category} onChange={(event) => update("category", event.target.value)}>
              <option value="">All categories</option>
              {(data?.categories ?? []).map((item) => (
                <option key={item} value={item}>
                  {humanise(item)}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span className="field__label">Type</span>
            <select className="select" value={kind} onChange={(event) => update("kind", event.target.value)}>
              <option value="">India &amp; international</option>
              <option value="DOMESTIC">India</option>
              <option value="INTERNATIONAL">International</option>
            </select>
          </label>
          <button type="button" className="btn btn--primary" onClick={() => update("q", query)}>
            <Search size={16} aria-hidden="true" /> Search packages
          </button>
          <button
            type="button"
            className="btn btn--ghost"
            onClick={() => {
              setQuery("");
              setParams(new URLSearchParams(), { replace: true });
            }}
          >
            <Filter size={16} aria-hidden="true" /> Clear filters
          </button>
        </div>
      </div>

      <div className="spread mb-2">
        <h2 className="mb-0">{filtered.length} package{filtered.length === 1 ? "" : "s"}</h2>
        <span className="badge">{data?.source === "DEMO_CATALOG" ? "Sample catalog" : data?.source ?? ""}</span>
      </div>

      {loading && <LoadingBlock label="Loading the catalog" />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && filtered.length === 0 && (
        <EmptyState title="No packages match those filters" icon={<Compass size={30} aria-hidden="true" />}>
          <p>Try clearing a filter or searching for a broader region such as “Kerala” or “Rajasthan”.</p>
        </EmptyState>
      )}

      <div className="grid grid--3 mb-4">
        {!loading &&
          filtered.map((pkg) => (
            <Link key={pkg.slug} to={`/holidays/${pkg.slug}`} className="media-card card--hover">
              <img src={pkg.imageUrl} alt={`Illustration for ${pkg.title}`} loading="lazy" />
              <div className="media-card__body">
                <div className="row row--wrap" style={{ gap: "0.4rem" }}>
                  <span className="badge badge--lime">{pkg.categoryLabel}</span>
                  <span className="badge">
                    {pkg.durationDays}D / {pkg.durationNights}N
                  </span>
                  {pkg.kind === "INTERNATIONAL" && <span className="badge badge--info">International</span>}
                </div>
                <h3 className="mb-0">{pkg.title}</h3>
                <p className="small muted mb-0">{pkg.summary}</p>
                <div className="tiny muted row" style={{ gap: "0.3rem" }}>
                  <MapPin size={12} aria-hidden="true" /> {pkg.destinationName}, {pkg.stateOrRegion}
                </div>
                <div className="media-card__foot">
                  <div>
                    <div className="tiny muted">From, per traveller</div>
                    <div className="price-tag">{formatMoney(pkg.priceMinor, pkg.currency)}</div>
                  </div>
                  <span className="btn btn--ghost btn--sm">View itinerary</span>
                </div>
              </div>
            </Link>
          ))}
      </div>
    </div>
  );
}
