import type { ReactNode } from "react";
import { AlertTriangle, Info, Loader2, SearchX } from "lucide-react";
import type { ProviderInfo } from "../lib/types";

export function LoadingBlock({ label = "Loading" }: { label?: string }) {
  return (
    <div className="row muted" role="status" aria-live="polite" style={{ padding: "2rem 0" }}>
      <Loader2 size={18} className="spin" aria-hidden="true" />
      <span>{label}…</span>
    </div>
  );
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="notice notice--danger" role="alert">
      <AlertTriangle size={18} aria-hidden="true" />
      <div>
        <strong>We couldn’t load this.</strong>
        <p className="small mb-0">{message}</p>
        {onRetry && (
          <button type="button" className="btn btn--ghost btn--sm mt-1" onClick={onRetry}>
            Try again
          </button>
        )}
      </div>
    </div>
  );
}

export function EmptyState({
  title,
  children,
  icon,
}: {
  title: string;
  children?: ReactNode;
  icon?: ReactNode;
}) {
  return (
    <div className="empty">
      {icon ?? <SearchX size={30} aria-hidden="true" />}
      <h3>{title}</h3>
      {children && <div className="small muted">{children}</div>}
    </div>
  );
}

export function ProviderNotice({
  mode,
  message,
  sourceTimestamp,
  ticketNote,
  liveDataConnected,
  liveProviderConfigured,
  setupHint,
}: {
  mode?: string;
  message?: string;
  sourceTimestamp?: string;
  ticketNote?: string;
  /** Set to false when the category has no live provider connection. */
  liveDataConnected?: boolean;
  liveProviderConfigured?: boolean;
  setupHint?: string;
}) {
  if (!mode && !message) return null;
  const isDemo = mode === "DEMO";
  const liveDisconnected = liveDataConnected === false && !isDemo;
  const title = isDemo
    ? "Demo sample data"
    : liveDisconnected
      ? "Live data not connected"
      : mode === "LIVE"
        ? "Live provider"
        : mode ?? "Provider status";

  return (
    <div className={`notice ${isDemo || liveDisconnected ? "notice--warn" : "notice--info"}`}>
      <Info size={18} aria-hidden="true" />
      <div>
        <strong>{title}</strong>
        <p className="small mb-0">
          {message ?? "Sample data is used for this provider."}
          {ticketNote ? ` ${ticketNote}` : ""}
        </p>
        {liveDisconnected && (
          <details className="mt-1">
            <summary className="small">How to connect live bus inventory</summary>
            <p className="tiny muted mb-0">
              {liveProviderConfigured
                ? "Bus credentials are present in the backend, but this build has no live bus adapter installed. "
                : "No live bus provider is connected. "}
              {setupHint ??
                "Install a backend bus inventory adapter, then set BUS_PROVIDER_MODE=LIVE and the BUS_LIVE_* credentials as backend environment variables. Never expose credentials to React. See README → Connecting live bus inventory."}
            </p>
          </details>
        )}
        {sourceTimestamp && <p className="tiny muted mb-0">Source timestamp: {sourceTimestamp}</p>}
      </div>
    </div>
  );
}

export function ProviderHealthNotice({ provider }: { provider: ProviderInfo }) {
  return (
    <div className="card card--pad">
      <div className="spread">
        <strong>{provider.displayName}</strong>
        <span className={`badge ${provider.effectiveMode === "DEMO" ? "badge--warn" : "badge--ok"}`}>
          {provider.effectiveMode}
        </span>
      </div>
      <p className="small muted mt-1 mb-1">{provider.message}</p>
      <p className="tiny muted mb-0">
        {provider.category} · configured {provider.configuredMode} · booking{" "}
        {provider.bookingSupported ? "supported" : "unavailable"}
      </p>
    </div>
  );
}
