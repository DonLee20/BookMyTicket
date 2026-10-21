import { Link } from "react-router-dom";
import { Compass } from "lucide-react";

export function NotFoundPage() {
  return (
    <div className="container section">
      <div className="empty">
        <Compass size={38} aria-hidden="true" />
        <h1>That page took a different route</h1>
        <p className="muted" style={{ maxWidth: "52ch", marginInline: "auto" }}>
          The link may be old, or the page may have moved. Start from search, or open one of the travel desks below.
        </p>
        <div className="row row--wrap mt-3" style={{ justifyContent: "center" }}>
          <Link to="/" className="btn btn--primary">
            Back to home
          </Link>
          <Link to="/buses" className="btn btn--ghost">
            Search buses
          </Link>
          <Link to="/bookings" className="btn btn--ghost">
            My bookings
          </Link>
        </div>
      </div>
    </div>
  );
}
