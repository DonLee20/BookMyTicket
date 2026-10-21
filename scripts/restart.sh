#!/usr/bin/env bash
# Rebuilds changed images and restarts services. Never deletes the MySQL volume.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

bash scripts/bootstrap-env.sh

echo "==> Rebuilding and restarting (data volume preserved)..."
docker compose up -d --build --wait
docker compose ps
echo "==> Restart complete."
