import { useState } from "react";
import { Link } from "react-router-dom";
import type { ReactNode } from "react";
import { Activity, BusFront, CalendarDays, Mail, RefreshCw, Users } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { ErrorState, LoadingBlock, ProviderHealthNotice } from "../components/Feedback";
import { StatusBadge } from "../components/Badges";
import { useAsync } from "../hooks/useAsync";
import { useToast } from "../context/ToastContext";
import { api, ApiError } from "../lib/api";
import { formatDateLong, formatDateTime, formatMoney, humanise, todayIso } from "../lib/format";
import type {
  AdminBusInventoryRow,
  AdminHealth,
  AdminMessageRow,
  AdminOverview,
  AdminPackageRow,
  AdminPaymentRow,
  AdminUserRow,
  Booking,
  Page,
} from "../lib/types";

type Tab = "overview" | "bookings" | "payments" | "users" | "packages" | "inventory" | "messages" | "health";

const tabs: { id: Tab; label: string; icon: typeof Activity }[] = [
  { id: "overview", label: "Overview", icon: Activity },
  { id: "bookings", label: "Bookings", icon: CalendarDays },
  { id: "payments", label: "Payments", icon: RefreshCw },
  { id: "users", label: "Users", icon: Users },
  { id: "packages", label: "Packages", icon: CalendarDays },
  { id: "inventory", label: "Bus inventory", icon: BusFront },
  { id: "messages", label: "Messages", icon: Mail },
  { id: "health", label: "Provider health", icon: Activity },
];

export function AdminPage() {
  const [tab, setTab] = useState<Tab>("overview");
  const overview = useAsync<AdminOverview>((signal) => api.get("/api/admin/overview", signal), []);

  return (
    <div className="container">
      <PageHead
        eyebrow="Operations"
        title="Admin dashboard"
        lede="Live operational view of bookings, payments, catalogue state and provider boundaries. Access is enforced server-side with ROLE_ADMIN."
        crumbs={[{ to: "/", label: "Home" }, { label: "Admin" }]}
        actions={
          <button
            type="button"
            className="btn btn--ghost btn--sm"
            onClick={overview.reload}
            aria-label="Refresh overview"
          >
            <RefreshCw size={15} aria-hidden="true" /> Refresh
          </button>
        }
      />

      <div className="admin-tabs" role="tablist">
        {tabs.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            type="button"
            role="tab"
            aria-selected={tab === id}
            className={tab === id ? "active" : ""}
            onClick={() => setTab(id)}
          >
            <Icon size={14} aria-hidden="true" /> {label}
          </button>
        ))}
      </div>

      {tab === "overview" && <OverviewTab state={overview} />}
      {tab === "bookings" && <BookingsTab />}
      {tab === "payments" && <PaymentsTab />}
      {tab === "users" && <UsersTab />}
      {tab === "packages" && <PackagesTab />}
      {tab === "inventory" && <InventoryTab />}
      {tab === "messages" && <MessagesTab />}
      {tab === "health" && <HealthTab />}
    </div>
  );
}

function OverviewTab({ state }: { state: ReturnType<typeof useAsync<AdminOverview>> }) {
  const { data, loading, error, reload } = state;
  if (loading) return <LoadingBlock label="Loading the overview" />;
  if (error || !data) return <ErrorState message={error ?? "Overview unavailable."} onRetry={reload} />;

  const statusEntries = Object.entries(data.bookingsByStatus);
  const categoryEntries = Object.entries(data.bookingsByCategory);
  const maxStatus = Math.max(1, ...statusEntries.map(([, value]) => value));
  const maxCategory = Math.max(1, ...categoryEntries.map(([, value]) => value));

  return (
    <div className="mb-4">
      <div className="grid grid--4">
        <Stat label="Total bookings" value={String(data.totalBookings)} />
        <Stat label="Confirmed" value={String(data.confirmedBookings)} />
        <Stat label="Revenue (demo)" value={data.revenueDisplay} />
        <Stat label="Pending payments" value={String(data.pendingPayments)} />
        <Stat label="Users" value={String(data.totalUsers)} />
        <Stat label="Active packages" value={`${data.activePackages} / ${data.totalPackages}`} />
        <Stat label="Bus trips today" value={String(data.busTripsToday)} />
        <Stat label="Seats open today" value={String(data.busSeatsAvailableToday)} />
      </div>

      <div className="grid grid--2 mt-3">
        <div className="card card--pad">
          <h2>Bookings by status</h2>
          {statusEntries.length === 0 ? (
            <p className="small muted">No bookings yet.</p>
          ) : (
            <div className="chart-bars mt-2">
              {statusEntries.map(([status, value]) => (
                <div key={status} className="chart-bar">
                  <span>{humanise(status)}</span>
                  <span className="chart-bar__track">
                    <span className="chart-bar__fill" style={{ width: `${(value / maxStatus) * 100}%` }} />
                  </span>
                  <strong>{value}</strong>
                </div>
              ))}
            </div>
          )}
        </div>
        <div className="card card--pad">
          <h2>Bookings by category</h2>
          {categoryEntries.length === 0 ? (
            <p className="small muted">No bookings yet.</p>
          ) : (
            <div className="chart-bars mt-2">
              {categoryEntries.map(([category, value]) => (
                <div key={category} className="chart-bar">
                  <span>{humanise(category)}</span>
                  <span className="chart-bar__track">
                    <span
                      className="chart-bar__fill"
                      style={{ width: `${(value / maxCategory) * 100}%`, background: "var(--charcoal)" }}
                    />
                  </span>
                  <strong>{value}</strong>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="notice notice--info mt-3">
        <Activity size={17} aria-hidden="true" />
        <span className="small">
          Payment provider mode: <strong>{data.paymentProviderMode}</strong>. Revenue is a demo total and does not
          represent money received. Generated {formatDateTime(data.generatedAt)}.
        </span>
      </div>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="stat">
      <div className="stat__value">{value}</div>
      <div className="stat__label">{label}</div>
    </div>
  );
}

function BookingsTab() {
  const { notify } = useToast();
  const [status, setStatus] = useState("");
  const [category, setCategory] = useState("");
  const [page, setPage] = useState(0);
  const query = new URLSearchParams({ page: String(page), size: "20" });
  if (status) query.set("status", status);
  if (category) query.set("category", category);

  const { data, loading, error, reload } = useAsync<Page<Booking>>(
    (signal) => api.get(`/api/admin/bookings?${query.toString()}`, signal),
    [status, category, page],
  );

  const cancel = async (booking: Booking) => {
    if (!window.confirm(`Cancel booking ${booking.id}? This is recorded in the audit trail.`)) return;
    try {
      await api.post(`/api/admin/bookings/${booking.id}/cancel`, { reason: "Cancelled by administrator from dashboard." });
      notify(`Booking ${booking.id} cancelled.`, "ok");
      reload();
    } catch (err) {
      notify(err instanceof ApiError ? err.message : "Cancellation failed.", "error");
    }
  };

  return (
    <div className="mb-4">
      <div className="row row--wrap mb-2">
        <label className="field" style={{ minWidth: 200 }}>
          <span className="field__label">Status</span>
          <select className="select" value={status} onChange={(event) => { setStatus(event.target.value); setPage(0); }}>
            <option value="">All statuses</option>
            {["PENDING", "PAYMENT_PENDING", "CONFIRMED", "CANCELLED", "REFUNDED", "FAILED", "EXPIRED"].map((value) => (
              <option key={value} value={value}>{humanise(value)}</option>
            ))}
          </select>
        </label>
        <label className="field" style={{ minWidth: 200 }}>
          <span className="field__label">Category</span>
          <select className="select" value={category} onChange={(event) => { setCategory(event.target.value); setPage(0); }}>
            <option value="">All categories</option>
            {["BUS", "PREMIUM_BUS", "TRAIN", "FLIGHT", "HOLIDAY"].map((value) => (
              <option key={value} value={value}>{humanise(value)}</option>
            ))}
          </select>
        </label>
      </div>
      {loading && <LoadingBlock />}
      {error && <ErrorState message={error} onRetry={reload} />}
      {data && (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr><th>Booking</th><th>Category</th><th>Status</th><th>Payment</th><th>Amount</th><th>Created</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {data.items.map((booking) => (
                  <tr key={booking.id}>
                    <td><strong>{booking.id}</strong><div className="tiny muted">{booking.contactEmail}</div></td>
                    <td>{booking.categoryLabel}</td>
                    <td><StatusBadge status={booking.status} label={booking.statusLabel} /></td>
                    <td><StatusBadge status={booking.paymentStatus} /></td>
                    <td>{booking.totalDisplay}</td>
                    <td className="tiny">{formatDateTime(booking.createdAt)}</td>
                    <td>
                      <div className="row" style={{ gap: "0.3rem" }}>
                        <Link to={`/bookings/${booking.id}`} className="btn btn--ghost btn--sm">View</Link>
                        {["PENDING", "PAYMENT_PENDING", "CONFIRMED"].includes(booking.status) && (
                          <button type="button" className="btn btn--danger btn--sm" onClick={() => cancel(booking)}>Cancel</button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pager page={data.page} hasNext={data.hasNext} total={data.total} onChange={setPage} />
        </>
      )}
    </div>
  );
}

function PaymentsTab() {
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const query = new URLSearchParams({ page: String(page), size: "20" });
  if (status) query.set("status", status);

  const { data, loading, error, reload } = useAsync<Page<AdminPaymentRow>>(
    (signal) => api.get(`/api/admin/payments?${query.toString()}`, signal),
    [status, page],
  );

  return (
    <div className="mb-4">
      <label className="field" style={{ maxWidth: 240 }}>
        <span className="field__label">Payment status</span>
        <select className="select" value={status} onChange={(event) => { setStatus(event.target.value); setPage(0); }}>
          <option value="">All statuses</option>
          {["PENDING", "PAID", "FAILED", "REFUNDED"].map((value) => (
            <option key={value} value={value}>{humanise(value)}</option>
          ))}
        </select>
      </label>
      {loading && <LoadingBlock />}
      {error && <ErrorState message={error} onRetry={reload} />}
      {data && (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Payment</th><th>Booking</th><th>User</th><th>Mode</th><th>Status</th><th>Amount</th><th>Created</th></tr></thead>
              <tbody>
                {data.items.map((payment) => (
                  <tr key={payment.id}>
                    <td><strong>{payment.id}</strong><div className="tiny muted">{payment.providerOrderId}</div></td>
                    <td><Link to={`/bookings/${payment.bookingId}`} className="link">{payment.bookingId}</Link></td>
                    <td>{payment.userEmail}</td>
                    <td><span className="badge">{payment.mode}</span></td>
                    <td><StatusBadge status={payment.status} /></td>
                    <td>{payment.amountDisplay}</td>
                    <td className="tiny">{formatDateTime(payment.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pager page={data.page} hasNext={data.hasNext} total={data.total} onChange={setPage} />
        </>
      )}
    </div>
  );
}

function UsersTab() {
  const [query, setQuery] = useState("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const params = new URLSearchParams({ page: String(page), size: "20" });
  if (search) params.set("query", search);

  const { data, loading, error, reload } = useAsync<Page<AdminUserRow>>(
    (signal) => api.get(`/api/admin/users?${params.toString()}`, signal),
    [search, page],
  );

  return (
    <div className="mb-4">
      <form
        className="row mb-2"
        onSubmit={(event) => {
          event.preventDefault();
          setSearch(query);
          setPage(0);
        }}
      >
        <input className="input" style={{ maxWidth: 320 }} placeholder="Search name or email" value={query} onChange={(event) => setQuery(event.target.value)} />
        <button type="submit" className="btn btn--primary btn--sm">Search</button>
      </form>
      {loading && <LoadingBlock />}
      {error && <ErrorState message={error} onRetry={reload} />}
      {data && (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>User</th><th>Email</th><th>Phone</th><th>Roles</th><th>Enabled</th><th>Joined</th></tr></thead>
              <tbody>
                {data.items.map((user) => (
                  <tr key={user.id}>
                    <td>{user.name}</td>
                    <td>{user.email}</td>
                    <td>{user.phone ?? "—"}</td>
                    <td>{user.roles.map((role) => <span key={role} className="badge">{role.replace("ROLE_", "")}</span>)}</td>
                    <td>{user.enabled ? "Yes" : "No"}</td>
                    <td className="tiny">{formatDateTime(user.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pager page={data.page} hasNext={data.hasNext} total={data.total} onChange={setPage} />
        </>
      )}
    </div>
  );
}

function PackagesTab() {
  const { notify } = useToast();
  const { data, loading, error, reload } = useAsync<AdminPackageRow[]>((signal) => api.get("/api/admin/packages", signal), []);

  const toggle = async (pkg: AdminPackageRow, patch: { active?: boolean; featured?: boolean }) => {
    try {
      await api.patch(`/api/admin/packages/${pkg.id}`, patch);
      notify(`Updated ${pkg.title}.`, "ok");
      reload();
    } catch (err) {
      notify(err instanceof ApiError ? err.message : "Could not update the package.", "error");
    }
  };

  if (loading) return <LoadingBlock />;
  if (error || !data) return <ErrorState message={error ?? "Packages unavailable."} onRetry={reload} />;

  return (
    <div className="table-wrap mb-4">
      <table className="table">
        <thead><tr><th>Package</th><th>Destination</th><th>Duration</th><th>Price</th><th>Active</th><th>Featured</th></tr></thead>
        <tbody>
          {data.map((pkg) => (
            <tr key={pkg.id}>
              <td><strong>{pkg.title}</strong><div className="tiny muted">{pkg.slug}</div></td>
              <td>{pkg.destinationName}</td>
              <td>{pkg.durationDays}D</td>
              <td>{pkg.priceDisplay}</td>
              <td>
                <button type="button" className={`badge ${pkg.active ? "badge--ok" : "badge--danger"}`} onClick={() => toggle(pkg, { active: !pkg.active })}>
                  {pkg.active ? "Active" : "Inactive"}
                </button>
              </td>
              <td>
                <button type="button" className={`badge ${pkg.featured ? "badge--lime" : ""}`} onClick={() => toggle(pkg, { featured: !pkg.featured })}>
                  {pkg.featured ? "Featured" : "Standard"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function InventoryTab() {
  const [date, setDate] = useState(todayIso());
  const [premiumOnly, setPremiumOnly] = useState(false);
  const { data, loading, error, reload } = useAsync<AdminBusInventoryRow[]>(
    (signal) => api.get(`/api/admin/bus-inventory?date=${date}&premiumOnly=${premiumOnly}`, signal),
    [date, premiumOnly],
  );

  return (
    <div className="mb-4">
      <div className="row row--wrap mb-2">
        <label className="field" style={{ maxWidth: 220 }}>
          <span className="field__label">Travel date</span>
          <input type="date" className="input" value={date} onChange={(event) => setDate(event.target.value)} />
        </label>
        <label className="checkbox" style={{ marginTop: "1.6rem" }}>
          <input type="checkbox" checked={premiumOnly} onChange={(event) => setPremiumOnly(event.target.checked)} />
          <span className="small">Premium coaches only</span>
        </label>
      </div>
      {loading && <LoadingBlock />}
      {error && <ErrorState message={error} onRetry={reload} />}
      {data && (
        <div className="table-wrap">
          <table className="table">
            <thead><tr><th>Trip</th><th>Route</th><th>Date</th><th>Departs</th><th>Fare</th><th>Seats</th><th>Status</th></tr></thead>
            <tbody>
              {data.map((row) => (
                <tr key={row.tripId}>
                  <td><strong>{row.operatorName}</strong><div className="tiny muted">#{row.tripId} · {row.busType.replaceAll("_", " ").toLowerCase()} {row.premium ? "· premium" : ""}</div></td>
                  <td>{row.departureCity} → {row.arrivalCity}</td>
                  <td>{formatDateLong(row.travelDate)}</td>
                  <td>{row.departTime}</td>
                  <td>{row.fareDisplay}</td>
                  <td>{row.availableSeats} / {row.totalSeats}</td>
                  <td><span className="badge">{row.status}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function MessagesTab() {
  const { notify } = useToast();
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const query = new URLSearchParams({ page: String(page), size: "20" });
  if (status) query.set("status", status);

  const { data, loading, error, reload } = useAsync<Page<AdminMessageRow>>(
    (signal) => api.get(`/api/admin/messages?${query.toString()}`, signal),
    [status, page],
  );

  const handle = async (message: AdminMessageRow) => {
    try {
      await api.post(`/api/admin/messages/${message.id}/handle`);
      notify("Message marked as handled.", "ok");
      reload();
    } catch (err) {
      notify(err instanceof ApiError ? err.message : "Could not update the message.", "error");
    }
  };

  return (
    <div className="mb-4">
      <label className="field" style={{ maxWidth: 220 }}>
        <span className="field__label">Status</span>
        <select className="select" value={status} onChange={(event) => { setStatus(event.target.value); setPage(0); }}>
          <option value="">All messages</option>
          <option value="NEW">New</option>
          <option value="HANDLED">Handled</option>
        </select>
      </label>
      {loading && <LoadingBlock />}
      {error && <ErrorState message={error} onRetry={reload} />}
      {data?.items.map((message) => (
        <div key={message.id} className="card card--pad mt-2">
          <div className="spread">
            <strong>{message.subject}</strong>
            <div className="row">
              <StatusBadge status={message.status} />
              {message.status !== "HANDLED" && (
                <button type="button" className="btn btn--ghost btn--sm" onClick={() => handle(message)}>
                  Mark handled
                </button>
              )}
            </div>
          </div>
          <div className="tiny muted">{message.name} · {message.email} · {formatDateTime(message.createdAt)}</div>
          <p className="small mt-1 mb-0">{message.message}</p>
        </div>
      ))}
      {data && data.items.length === 0 && <p className="small muted">No messages in this view.</p>}
      {data && <Pager page={data.page} hasNext={data.hasNext} total={data.total} onChange={setPage} />}
    </div>
  );
}

function HealthTab() {
  const { data, loading, error, reload } = useAsync<AdminHealth>((signal) => api.get("/api/admin/health", signal), []);
  if (loading) return <LoadingBlock />;
  if (error || !data) return <ErrorState message={error ?? "Health unavailable."} onRetry={reload} />;

  return (
    <div className="mb-4">
      <div className="grid grid--4">
        <Stat label="Application" value={data.status} />
        <Stat label="Database" value={data.database} />
        <Stat label="Schema version" value={data.schemaVersion} />
        <Stat label="Uptime (s)" value={String(data.uptimeSeconds)} />
      </div>
      <div className="grid grid--2 mt-3">
        {data.providers.map((provider) => (
          <ProviderHealthNotice key={provider.id} provider={provider} />
        ))}
      </div>
      <div className="card card--pad mt-3">
        <h2>Maintenance summary</h2>
        <div className="grid grid--4 mt-2">
          <Stat label="Users" value={String(data.maintenance.totalUsers)} />
          <Stat label="Bookings" value={String(data.maintenance.totalBookings)} />
          <Stat label="Payments" value={String(data.maintenance.totalPayments)} />
          <Stat label="Provider calls" value={String(data.maintenance.totalProviderCalls)} />
        </div>
        <p className="tiny muted mt-2 mb-0">
          Payment mode {data.paymentProviderMode} · server time {formatDateTime(data.serverTime)}
        </p>
      </div>
    </div>
  );
}

function Pager({
  page,
  hasNext,
  total,
  onChange,
}: {
  page: number;
  hasNext: boolean;
  total: number;
  onChange: (page: number) => void;
}) {
  return (
    <div className="row mt-2" style={{ justifyContent: "flex-end" }}>
      <span className="tiny muted">{total} records</span>
      <button type="button" className="btn btn--ghost btn--sm" disabled={page === 0} onClick={() => onChange(page - 1)}>
        Previous
      </button>
      <span className="small">Page {page + 1}</span>
      <button type="button" className="btn btn--ghost btn--sm" disabled={!hasNext} onClick={() => onChange(page + 1)}>
        Next
      </button>
    </div>
  );
}

export function AdminCurrency({ minor, currency }: { minor: number; currency: string }): ReactNode {
  return <>{formatMoney(minor, currency)}</>;
}
