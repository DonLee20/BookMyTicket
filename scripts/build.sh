#!/usr/bin/env bash
# Builds the production frontend bundle and the backend jar inside Docker.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

bash scripts/bootstrap-env.sh

echo "==> Building backend image..."
docker compose build travel-backend

echo "==> Building frontend image..."
docker compose build travel-frontend

echo "==> Build complete."
