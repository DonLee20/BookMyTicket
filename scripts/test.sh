#!/usr/bin/env bash
# Automated test entry point.
#
# 1. Starts a throwaway MySQL container (host network, port 13307).
# 2. Runs backend tests (JUnit + Flyway + real MySQL) in a Maven container.
# 3. Runs frontend unit tests with Vitest on the host via Bun.
#
# The throwaway database is deleted afterwards. The application MySQL volume is
# never touched.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

TEST_MYSQL_CONTAINER="bmt-test-mysql-$$"
TEST_DB_PORT=13307
TEST_DB_NAME=bookmyticket_test
TEST_DB_USER=bmt_test
TEST_DB_PASSWORD=bmt_test_pw

cleanup() {
  docker rm -f "$TEST_MYSQL_CONTAINER" >/dev/null 2>&1 || true
}
trap cleanup EXIT

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker is required to run the test suite." >&2
  exit 1
fi

echo "==> Starting throwaway MySQL for backend tests..."
docker run -d --rm --name "$TEST_MYSQL_CONTAINER" \
  --network host \
  -e MYSQL_DATABASE="$TEST_DB_NAME" \
  -e MYSQL_USER="$TEST_DB_USER" \
  -e MYSQL_PASSWORD="$TEST_DB_PASSWORD" \
  -e MYSQL_ROOT_PASSWORD=root_pw \
  mysql:8.4 --port="$TEST_DB_PORT" >/dev/null

echo "==> Waiting for MySQL to accept connections..."
for _ in $(seq 1 60); do
  if docker exec "$TEST_MYSQL_CONTAINER" mysqladmin ping -h 127.0.0.1 -P "$TEST_DB_PORT" -uroot -proot_pw --silent >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

echo "==> Running backend tests..."
docker run --rm \
  --network host \
  -v "$ROOT_DIR/backend":/app \
  -v bmt-maven-repo:/root/.m2 \
  -w /app \
  -e TEST_DB_URL="jdbc:mysql://127.0.0.1:${TEST_DB_PORT}/${TEST_DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e TEST_DB_USER="$TEST_DB_USER" \
  -e TEST_DB_PASSWORD="$TEST_DB_PASSWORD" \
  maven:3.9-eclipse-temurin-21 mvn -q -B test

echo "==> Installing frontend dependencies..."
bun install --frozen-lockfile 2>/dev/null || bun install

echo "==> Running frontend tests..."
(cd frontend && bun run test)

echo "==> All tests passed."
