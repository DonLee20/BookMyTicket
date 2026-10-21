import type { ReactNode } from "react";
import { Link } from "react-router-dom";

export function PageHead({
  eyebrow,
  title,
  lede,
  crumbs,
  actions,
}: {
  eyebrow?: string;
  title: string;
  lede?: string;
  crumbs?: { to?: string; label: string }[];
  actions?: ReactNode;
}) {
  return (
    <div className="page-head">
      {crumbs && (
        <nav className="breadcrumbs" aria-label="Breadcrumb">
          {crumbs.map((crumb, index) => (
            <span key={`${crumb.label}-${index}`}>
              {crumb.to ? <Link to={crumb.to}>{crumb.label}</Link> : crumb.label}
              {index < crumbs.length - 1 ? " / " : ""}
            </span>
          ))}
        </nav>
      )}
      <div className="spread">
        <div>
          {eyebrow && <div className="eyebrow mb-2">{eyebrow}</div>}
          <h1 className="mb-2">{title}</h1>
          {lede && <p className="lede mb-0">{lede}</p>}
        </div>
        {actions && <div className="row row--wrap">{actions}</div>}
      </div>
    </div>
  );
}
