#!/usr/bin/env bash
# Runs Flyway migrations against the Docker MySQL without starting the app web
# server. Safe to run repeatedly; data is preserved.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

bash scripts/bootstrap-env.sh

echo "==> Starting MySQL..."
docker compose up -d travel-mysql
docker compose wait travel-mysql 2>/dev/null || true

echo "==> Running Flyway migrations (migrate-only backend run)..."
docker compose run --rm \
  -e APP_MIGRATE_ONLY=true \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE=none \
  travel-backend

echo "==> Migrations complete."
