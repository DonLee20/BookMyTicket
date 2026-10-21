#!/usr/bin/env bash
# Primary developer entry point:  bun install && bun run dev
#
# Builds images when necessary (Docker layer cache), starts MySQL, backend and
# the Nginx-served frontend, waits for health checks and prints the URL.
# Containers and the MySQL volume are reused on subsequent runs.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker is not available. This project runs entirely in Docker." >&2
  echo "Start Docker (e.g. 'systemctl --user start docker-desktop') and retry." >&2
  exit 1
fi

bash scripts/bootstrap-env.sh

echo "==> Building images (cached layers are reused) and starting services..."
docker compose up -d --build --wait

# shellcheck disable=SC1091
FRONTEND_PORT="$(grep -E '^FRONTEND_PORT=' .env | cut -d= -f2 | tr -d '[:space:]')"
FRONTEND_PORT="${FRONTEND_PORT:-8080}"
BACKEND_PORT="$(grep -E '^BACKEND_PORT=' .env | cut -d= -f2 | tr -d '[:space:]')"
BACKEND_PORT="${BACKEND_PORT:-8081}"

echo
echo "==> BookMyTicket is running"
echo "    Web app:  http://localhost:${FRONTEND_PORT}"
echo "    API:      http://localhost:${FRONTEND_PORT}/api"
echo "    Health:   http://localhost:${BACKEND_PORT}/actuator/health"
echo "    Logs:     bun run logs"
