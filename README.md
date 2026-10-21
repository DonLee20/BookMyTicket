# BookMyTicket

A unified travel booking desk for **buses, premium coaches, Indian Railways search, flights, Chennai MTC route planning and holiday packages** — built to be explicit about what is demo data and what is a real provider handoff.

> **This is a demonstration application.** Bus, flight and holiday inventory is sample data. Rail reservations are handed off to the official IRCTC portal. MTC route information is for planning only. Payments are simulated end-to-end and never charge a real card or bank account.

---

## Stack

| Layer | Technology |
| --- | --- |
| Frontend | React 18, TypeScript, Vite, React Router, React Hook Form + Zod, Lucide icons |
| Backend | Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA, Flyway, springdoc OpenAPI |
| Database | MySQL 8.4 |
| Serving | Nginx (SPA + `/api` reverse proxy) |
| Tooling | Bun workspaces, Docker Compose, Vitest, JUnit 5 + MockMvc |

---

## Quick start

Prerequisites: Docker (Docker Desktop or Engine) and Bun.

```bash
bun install          # workspace dependencies
bun run dev          # build images, start MySQL/backend/frontend, wait for health
```

The first run creates `.env` from `.env.example` with generated local secrets and prints the demo administrator password once.

| Service | URL |
| --- | --- |
| Web app | http://localhost:8080 |
| API (through Nginx) | http://localhost:8080/api |
| Backend direct | http://localhost:8081 |
| API docs (Swagger UI) | http://localhost:8081/swagger-ui.html |
| Backend health | http://localhost:8081/actuator/health |

Stop the stack with `bun run clean` (data preserved) or remove everything with `bun run reset`.

---

## Features

- **Buses** — corridor search, filters/sorting, detailed seat maps, boarding/dropping points, temporary seat holds and demo checkout.
- **Premium coaches** — the same booking flow restricted to premium inventory, with a curated highlights page.
- **Trains** — sample timetables, running days, classes, quotas and availability notes. Booking creates a **request only** and hands off to IRCTC; no PNR or ticket is issued.
- **Flights** — demo offers across 45 airports, price revalidation before holding, cabin/stop filters and an explicit “ticket issuance unavailable in demo mode” contract.
- **Chennai MTC** — stage-by-stage planning across sample routes, stops, frequencies and fares, with an official-portal link for ticketing.
- **Holiday packages** — destination and package catalog, itineraries, inclusions/exclusions, departure capacity and demo payment.
- **Accounts** — cookie-based sessions, registration/login, profile and password management.
- **Bookings** — one list for every category, hold countdowns, demo payment completion/failure, cancellation and refund recording.
- **Admin dashboard** — overview metrics, bookings, payments, users, package editing, daily bus inventory, contact messages and provider health.
- **Honesty by design** — provider modes (`DEMO`, `EXTERNAL_HANDOFF`, `UNAVAILABLE`) are surfaced in the UI and API; the app never fabricates live tickets or silently falls back.

---

## Repository layout

```
backend/
  src/main/java/com/bookmyticket/
    admin/        admin API and dashboard queries
    booking/      booking creation, lifecycle, holds, cancellation
    catalog/      holiday destinations/packages
    common/       API errors, money/time helpers, pagination
    config/       properties, seeders, migration runner
    contact/      contact messages
    identity/     users, roles, auth, profile
    payment/      demo payment provider and webhook handling
    provider/     provider registry and demo adapters
    security/     Spring Security, CSRF, session auth
    transport/    bus, train, flight and MTC domains
  src/main/resources/db/migration/   Flyway schema + demo seeds
  src/test/                           JUnit/MockMvc integration tests
frontend/
  src/pages/        route-level screens
  src/components/   shared UI, forms, layout
  src/context/      auth and toast providers
  src/lib/          API client, types, formatting
  nginx.conf        SPA fallback and /api proxy
scripts/            dev/build/test/reset helpers
docker-compose.yml  MySQL + backend + frontend
```

---

## Commands

| Command | Description |
| --- | --- |
| `bun run dev` | Build and start everything, waiting for health checks. |
| `bun run up` | Start existing images without rebuilding. |
| `bun run build` | Build backend and frontend images. |
| `bun run restart` | Rebuild changed images and restart; data is preserved. |
| `bun run logs` | Follow container logs. |
| `bun run status` | Show container status. |
| `bun run test` | Run backend integration tests and frontend unit tests. |
| `bun run db:migrate` | Apply Flyway migrations without starting the API. |
| `bun run clean` | Stop containers, keep the MySQL volume. |
| `bun run reset` | **Destructive:** delete containers, the network and the MySQL volume. |

### Frontend-only development

With the backend running in Docker:

```bash
cd frontend
bun install
bun run dev      # http://localhost:5173, proxies /api to localhost:8081
```

Set `VITE_API_PROXY` to point at a different backend host if needed.

---

## Configuration

All configuration lives in `.env` (created from `.env.example`, git-ignored).

| Variable | Purpose |
| --- | --- |
| `MYSQL_*`, `MYSQL_PORT` | Database credentials and host port (default `3307`). |
| `FRONTEND_PORT`, `BACKEND_PORT` | Public ports (defaults `8080`, `8081`). |
| `ADMIN_EMAIL`, `ADMIN_NAME`, `ADMIN_PASSWORD` | Optional administrator seeded at startup. Leave the password blank to skip. |
| `DEMO_BUS_DAYS_AHEAD` | Startup window of generated demo bus trips (default 30 days). Other future dates are filled on demand when searched. |
| `BUS_PROVIDER_MODE`, `RAILWAY_PROVIDER_MODE`, `FLIGHT_PROVIDER_MODE`, `MTC_PROVIDER_MODE` | `DEMO`, `LIVE`, `EXTERNAL_HANDOFF` or `UNAVAILABLE`. Only demo/handoff behaviour is implemented in this release. |
| `BUS_LIVE_PROVIDER`, `BUS_LIVE_BASE_URL`, `BUS_LIVE_API_KEY`, `BUS_LIVE_API_SECRET` | Backend-only placeholders for an authorized live bus adapter. They are never sent to React. Credentials alone do not enable live results. |
| `PAYMENT_PROVIDER` | Only `demo` is implemented. |
| `PAYMENT_WEBHOOK_SECRET` | HMAC secret for signed demo payment webhooks. |
| `IRCTC_OFFICIAL_HANDOFF_URL`, `MTC_OFFICIAL_INFO_URL` | Official portals linked from the UI. |
| `AMADEUS_*`, `RAZORPAY_*` | Reserved for future live adapters; credentials alone never enable live booking. |

### Provider honesty model

The backend reports an effective provider mode for every search/booking response. When live credentials are absent or a live adapter is not implemented, the mode is `DEMO` or `UNAVAILABLE` — never silently upgraded. Rail bookings always return `EXTERNAL_HANDOFF` with the official IRCTC URL.

### Connecting live bus inventory

This build does **not** ship a live bus adapter. Bus search reads the local demo dataset through `BusSearchService`; `BusProvider` remains the server-side fare-authority boundary. Setting `BUS_PROVIDER_MODE=LIVE` without installing an adapter returns HTTP 503 (`BUSPROVIDER_UNAVAILABLE`) and never falls back to demo rows.

To connect an authorized bus inventory provider:

1. Obtain API access and the provider’s documented search/seat/booking contract.
2. Implement a backend adapter that maps provider inventory into the existing bus search and booking workflow. Never call the provider directly from React.
3. Put only backend-side values in `.env` (or the deployment secret store):
   `BUS_LIVE_PROVIDER`, `BUS_LIVE_BASE_URL`, `BUS_LIVE_API_KEY`, `BUS_LIVE_API_SECRET`.
4. Set `BUS_PROVIDER_MODE=LIVE`, restart the backend, and verify provider status in `/api/admin/health` (admin login required).
5. Exercise real provider searches from the backend. If the provider fails, return a provider error; do not substitute sample rows.

`BUS_PROVIDER_MODE=DEMO` remains available for development. Demo results stay labelled and expose `liveDataConnected: false`.

In `DEMO` mode every future route/date search is filled with generated sample inventory. The first search for an unseeded city pair creates a demo corridor, schedules, trips and seat maps for that date; search never returns an empty page merely because the route was not part of the original seed.

Bus search responses also include `diagnostics.status`, which separates:

- `OK` — sample coaches are available (seeded or generated on demand)
- `ORIGIN_NOT_COVERED` / `DESTINATION_NOT_COVERED` — only used when live inventory does not include a city
- `ROUTE_NOT_COVERED` — only used when live inventory has no direct route
- `OUTSIDE_DEMO_WINDOW` — retained for non-demo provider modes
- `NO_TRIPS_ON_DATE` — retained for non-demo provider modes
- `FILTERED_OUT` — trips exist, but none match the requested filters

Generated rows are always marked `source = DEMO` and the API keeps reporting `liveDataConnected: false`; sample data is never presented as live inventory. Train searches follow the same pattern: unknown stations and unseeded routes receive generated demo stations, services, classes and route stops for the selected date.

---

## Booking lifecycle

1. Search demo inventory and select seats/offers.
2. Submit a booking. Bus seats move to `HELD` with a **20-minute hold**; the booking status becomes `PAYMENT_PENDING` and a demo payment intent is created.
3. Complete or fail the demo payment from the booking page. Success moves the booking to `CONFIRMED`; failure is recorded without confirmation.
4. Cancellation is allowed from pending/confirmed states. Refunds are calculated from the demo policy (bus: 90% up to 24h before departure, 50% between 6–24h, none within 6h) and recorded against the simulated payment.
5. Expired holds are released by a scheduled job every minute.

Train requests are created with `NOT_REQUIRED` payment and `EXTERNAL_HANDOFF` next action. Flight demo requests are hold-only (`DEMO_HOLD_ONLY`) because no ticket issuance adapter exists.

---

## API

OpenAPI is served by the backend at `/v3/api-docs`; Swagger UI at `/swagger-ui.html`.

Main groups:

- `/api/auth/*` — register, login, logout, current user, CSRF bootstrap
- `/api/buses/*`, `/api/premium/*` — search, cities, amenities, seat maps
- `/api/trains/*` — stations, search
- `/api/flights/*` — airports, search, revalidate
- `/api/mtc/*` — stops, plan
- `/api/holidays/*` — home, packages, destinations
- `/api/bookings/*` — create by category, list, detail, cancel
- `/api/payments/*` — demo completion and signed webhook
- `/api/profile/*` — profile and password
- `/api/contact` — public enquiry form
- `/api/admin/*` — role-protected dashboard endpoints

Errors use a consistent payload:

```json
{
  "timestamp": "2026-10-20T04:15:35Z",
  "status": 404,
  "code": "NOT_FOUND",
  "message": "That resource could not be found.",
  "path": "/api/holidays/destinations",
  "fieldErrors": {}
}
```

Authentication is session-cookie based. Unsafe requests require the `X-XSRF-TOKEN` header matching the `XSRF-TOKEN` cookie; the SPA bootstraps this from `/api/auth/csrf` automatically. The API is same-origin behind Nginx, so no permissive CORS is enabled.

---

## Testing

```bash
bun run test
```

The script:

1. Starts a **throwaway MySQL 8.4 container** on port `13307` (never touches the app volume).
2. Runs the backend JUnit/MockMvc suite in a Maven container with `TEST_DB_*` pointed at that database. Flyway applies all migrations and a small demo inventory is seeded by the `test` profile.
3. Runs the frontend Vitest suite on the host with Bun.
4. Deletes the throwaway database.

Current coverage includes public catalog/search endpoints, generated demo inventory for unseeded bus routes and train stations, JSON 404s, CSRF enforcement, auth failures, duplicate registration, the complete bus register → search → hold → demo-pay → cancel flow, and the IRCTC handoff contract for trains.

Backend reports are written to `backend/target/surefire-reports/`.

---

## Security notes

- Passwords are BCrypt-hashed; sessions are HTTP-only, SameSite=Lax cookies.
- CSRF protection is enabled for all unsafe methods except the HMAC-signed payment webhook.
- Admin routes require `ROLE_ADMIN`; the seeded admin is only created when `ADMIN_PASSWORD` is configured.
- Provider credentials stay server-side. No secret is compiled into the frontend.
- The demo payment simulator deliberately refuses to represent itself as a real payment receipt.
- Nginx sends security headers and only proxies `/api` to the backend; Actuator stays on the internal/backend port.

---

## Troubleshooting

- **Docker not available** — start Docker Desktop/Engine first; `bun run dev` fails fast otherwise.
- **Port already in use** — change `FRONTEND_PORT`, `BACKEND_PORT` or `MYSQL_PORT` in `.env`.
- **Backend code changes not visible** — `bun run restart` rebuilds images; `bun run up` reuses existing images.
- **Database reset** — `bun run reset` deletes the MySQL volume; the next `bun run dev` migrates and seeds from scratch.
- **Fresh seed is slow?** Demo inventory is generated in one transaction; a 30-day window still creates thousands of trips, so the first startup takes noticeably longer than subsequent restarts.
- **Admin password lost** — set a new `ADMIN_PASSWORD` in `.env` and restart; the seeded administrator is updated idempotently.
