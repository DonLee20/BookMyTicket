import { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { Menu, Plane, TrainFront, BusFront, X } from "lucide-react";
import { useAuth } from "../context/AuthContext";

const primaryNav = [
  { to: "/explore", label: "Explore" },
  { to: "/buses", label: "Buses" },
  { to: "/trains", label: "Trains" },
  { to: "/flights", label: "Flights" },
  { to: "/chennai-mtc", label: "Chennai MTC" },
  { to: "/premium", label: "Premium Travel" },
  { to: "/holidays", label: "Holiday Packages" },
];

export function Layout() {
  const [open, setOpen] = useState(false);
  const { user, logout, loading } = useAuth();
  const location = useLocation();

  useEffect(() => {
    setOpen(false);
    window.scrollTo({ top: 0, behavior: "instant" as ScrollBehavior });
  }, [location.pathname, location.search]);

  return (
    <>
      <a className="skip-link" href="#main">
        Skip to content
      </a>
      <header className="site-header">
        <div className="container site-header__inner">
          <Link to="/" className="brand" aria-label="BookMyTicket home">
            <span className="brand__mark" aria-hidden="true">
              bm
            </span>
            BookMyTicket
          </Link>
          <nav className="nav" aria-label="Primary">
            {primaryNav.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) => `nav__link${isActive ? " active" : ""}`}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="header-actions">
            <Link to="/bookings" className="btn btn--ghost btn--sm hide-sm">
              My Bookings
            </Link>
            {!loading && (
              user ? (
                <Link to={user.roles.includes("ROLE_ADMIN") ? "/admin" : "/profile"} className="btn btn--primary btn--sm">
                  {user.name.split(" ")[0]}
                </Link>
              ) : (
                <Link to="/login" className="btn btn--primary btn--sm">
                  Sign In
                </Link>
              )
            )}
            <button
              type="button"
              className="menu-toggle"
              aria-expanded={open}
              aria-controls="mobile-nav"
              aria-label={open ? "Close menu" : "Open menu"}
              onClick={() => setOpen((value) => !value)}
            >
              {open ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>
        <div id="mobile-nav" className={`mobile-nav${open ? " open" : ""}`}>
          {primaryNav.map((item) => (
            <Link key={item.to} to={item.to}>
              {item.label}
            </Link>
          ))}
          <Link to="/bookings">My Bookings</Link>
          {user ? (
            <>
              <Link to={user.roles.includes("ROLE_ADMIN") ? "/admin" : "/profile"}>
                {user.roles.includes("ROLE_ADMIN") ? "Admin dashboard" : "My profile"}
              </Link>
              <button
                type="button"
                className="btn btn--ghost mt-2"
                onClick={() => {
                  void logout();
                }}
              >
                Sign out
              </button>
            </>
          ) : (
            <Link to="/login">Sign in</Link>
          )}
        </div>
      </header>

      <main id="main">
        <Outlet />
      </main>

      <Footer />
    </>
  );
}

function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer__grid">
          <div>
            <Link to="/" className="brand" style={{ color: "var(--cream)" }}>
              <span className="brand__mark" aria-hidden="true">
                bm
              </span>
              BookMyTicket
            </Link>
            <p className="small mt-2" style={{ maxWidth: "32ch", color: "#b9b5a9" }}>
              One considered place for buses, trains, flights, city transport and holidays across India and beyond.
            </p>
            <div className="row small" style={{ color: "#b9b5a9" }}>
              <BusFront size={16} /> <TrainFront size={16} /> <Plane size={16} />
              <span>Buses · Rail · Air · Holidays</span>
            </div>
          </div>
          <div>
            <div className="footer__title">Book</div>
            <div className="footer__links">
              <Link to="/buses">Buses</Link>
              <Link to="/premium">Premium coaches</Link>
              <Link to="/trains">Trains</Link>
              <Link to="/flights">Flights</Link>
              <Link to="/chennai-mtc">Chennai MTC</Link>
              <Link to="/holidays">Holiday packages</Link>
            </div>
          </div>
          <div>
            <div className="footer__title">Account</div>
            <div className="footer__links">
              <Link to="/bookings">My bookings</Link>
              <Link to="/profile">Profile</Link>
              <Link to="/login">Sign in</Link>
              <Link to="/register">Create account</Link>
              <Link to="/contact">Contact us</Link>
              <Link to="/explore">Explore destinations</Link>
            </div>
          </div>
          <div>
            <div className="footer__title">An honest booking desk</div>
            <p className="small" style={{ color: "#b9b5a9" }}>
              Demo inventory is labelled wherever it appears. Train and flight requests are samples — no PNR, ticket or
              live fare is issued. Demo payments are simulated and are never charged.
            </p>
            <p className="tiny" style={{ color: "#8f8b80" }}>
              © {new Date().getFullYear()} BookMyTicket. All sample data is fictional unless explicitly linked to an
              official provider.
            </p>
          </div>
        </div>
        <div className="footer__bottom">
          <span>Built for clarity, not for overpromising.</span>
          <span>Chennai, India · UTC records · INR pricing</span>
        </div>
      </div>
    </footer>
  );
}
