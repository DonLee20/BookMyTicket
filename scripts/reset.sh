#!/usr/bin/env bash
# DESTRUCTIVE: removes containers AND the MySQL data volume, then starts fresh.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

read -r -p "This deletes the bookmyticket MySQL volume and all local data. Type 'reset' to continue: " answer
if [[ "$answer" != "reset" ]]; then
  echo "Aborted."
  exit 1
fi

docker compose down -v --remove-orphans
echo "==> Volume removed. Run 'bun run dev' to rebuild a fresh environment."
