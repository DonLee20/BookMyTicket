import { statusTone } from "../lib/format";

export function StatusBadge({ status, label }: { status: string; label?: string }) {
  const tone = statusTone(status);
  const suffix =
    tone === "ok" ? "badge--ok" : tone === "warn" ? "badge--warn" : tone === "danger" ? "badge--danger" : tone === "info" ? "badge--info" : "";
  return <span className={`badge ${suffix}`}>{label ?? status.replaceAll("_", " ")}</span>;
}

export function DemoBadge({ mode }: { mode?: string }) {
  if (!mode) return null;
  if (mode === "DEMO") return <span className="badge badge--warn">Demo data</span>;
  if (mode === "LIVE") return <span className="badge badge--ok">Live</span>;
  if (mode === "EXTERNAL_HANDOFF") return <span className="badge badge--info">Official handoff</span>;
  return <span className="badge">{mode.replaceAll("_", " ")}</span>;
}
