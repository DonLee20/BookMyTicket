FROM oven/bun:1 AS build
WORKDIR /app
COPY frontend/package.json ./
RUN bun install
COPY frontend/ ./
RUN bun run build

FROM nginx:1.27-alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=6 \
  CMD wget -qO- http://127.0.0.1/healthz >/dev/null || exit 1
