#!/usr/bin/env bash
# Stops containers but keeps them and the MySQL volume intact.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

docker compose stop
echo "==> Stopped. Data preserved. Use 'bun run up' or 'bun run dev' to resume."
