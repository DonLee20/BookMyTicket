#!/usr/bin/env bash
# Creates .env from .env.example on first run and replaces placeholder secrets
# with random values. Never overwrites an existing .env.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -f .env ]]; then
  echo "==> .env already exists, keeping it."
  exit 0
fi

rand() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex 16
  else
    od -An -N16 -tx1 /dev/urandom | tr -d ' \n'
  fi
}

DB_PASSWORD="bmt_$(rand)"
ROOT_PASSWORD="bmt_root_$(rand)"
WEBHOOK_SECRET="whsec_$(rand)"
ADMIN_PASSWORD="Admin_$(rand | cut -c1-12)"

sed \
  -e "s|^MYSQL_PASSWORD=.*|MYSQL_PASSWORD=${DB_PASSWORD}|" \
  -e "s|^MYSQL_ROOT_PASSWORD=.*|MYSQL_ROOT_PASSWORD=${ROOT_PASSWORD}|" \
  -e "s|^PAYMENT_WEBHOOK_SECRET=.*|PAYMENT_WEBHOOK_SECRET=${WEBHOOK_SECRET}|" \
  -e "s|^ADMIN_PASSWORD=.*|ADMIN_PASSWORD=${ADMIN_PASSWORD}|" \
  .env.example > .env

chmod 600 .env
echo "==> Created .env with generated local secrets."
echo "    Demo admin login: $(grep '^ADMIN_EMAIL=' .env | cut -d= -f2) / ${ADMIN_PASSWORD}"
echo "    (Store this password somewhere safe; it is only printed once.)"
