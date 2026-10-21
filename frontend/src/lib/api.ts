import type { Page } from "./types";

export class ApiError extends Error {
  status: number;
  code: string;
  fieldErrors: Record<string, string>;

  constructor(status: number, code: string, message: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors ?? {};
  }
}

function readCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp(`(?:^|; )${name.replace(/([.$?*|{}()[\]\\/+^])/g, "\\$1")}=([^;]*)`));
  return match ? decodeURIComponent(match[1]) : null;
}

let csrfReady: Promise<void> | null = null;
let csrfToken: string | null = null;

/**
 * Ensures a CSRF token is available before the first unsafe request.
 * The token is read from the JSON bootstrap response so the SPA does not
 * depend on JavaScript being able to read the XSRF cookie (some proxies mark
 * cookies HttpOnly). Session auth itself remains cookie-based.
 */
export function ensureCsrf(): Promise<void> {
  if (csrfReady) return csrfReady;
  csrfReady = fetch("/api/auth/csrf", { credentials: "include" })
    .then(async (response) => {
      if (!response.ok) return;
      const payload = (await response.json().catch(() => null)) as { token?: unknown } | null;
      if (payload && typeof payload.token === "string") {
        csrfToken = payload.token;
      }
    })
    .catch(() => undefined);
  return csrfReady;
}

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  signal?: AbortSignal;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const method = options.method ?? "GET";
  const headers: Record<string, string> = { Accept: "application/json" };
  let usedToken: string | null = null;

  if (method !== "GET") {
    await ensureCsrf();
    usedToken = csrfToken ?? readCookie("XSRF-TOKEN");
    if (usedToken) headers["X-XSRF-TOKEN"] = usedToken;
  }
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(path, {
    method,
    headers,
    credentials: "include",
    signal: options.signal,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  let payload: unknown = null;
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = null;
    }
  }

  if (!response.ok) {
    const error = (payload ?? {}) as {
      code?: string;
      message?: string;
      fieldErrors?: Record<string, string>;
    };
    // A missing/expired CSRF cookie breaks every unsafe request; refresh once and retry.
    if (response.status === 403 && error.code === "FORBIDDEN" && !path.startsWith("/api/auth/csrf")) {
      csrfReady = null;
      csrfToken = null;
      await ensureCsrf();
      const retryToken = csrfToken ?? readCookie("XSRF-TOKEN");
      if (retryToken && retryToken !== usedToken) {
        return apiRequest<T>(path, options);
      }
    }
    throw new ApiError(
      response.status,
      error.code ?? "REQUEST_FAILED",
      error.message ?? "The request could not be completed. Please try again.",
      error.fieldErrors,
    );
  }

  return payload as T;
}

export const api = {
  get: <T>(path: string, signal?: AbortSignal) => apiRequest<T>(path, { signal }),
  post: <T>(path: string, body?: unknown, signal?: AbortSignal) =>
    apiRequest<T>(path, { method: "POST", body, signal }),
  put: <T>(path: string, body?: unknown) => apiRequest<T>(path, { method: "PUT", body }),
  patch: <T>(path: string, body?: unknown) => apiRequest<T>(path, { method: "PATCH", body }),
};

export type { Page };

export function newIdempotencyKey(): string {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `key-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}
