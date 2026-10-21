import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

describe("api CSRF bootstrap", () => {
  beforeEach(() => {
    vi.resetModules();
    vi.restoreAllMocks();
    document.cookie = "XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/";
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("uses the token from the bootstrap response when the cookie is not JS-readable", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ token: "csrf-from-body", headerName: "X-XSRF-TOKEN" }), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ trips: [] }), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }),
      );
    vi.stubGlobal("fetch", fetchMock);

    const { api } = await import("./api");
    await api.post("/api/buses/search", { from: "Chennai" });

    expect(fetchMock).toHaveBeenCalledTimes(2);
    const [csrfUrl] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(csrfUrl).toBe("/api/auth/csrf");

    const [url, init] = fetchMock.mock.calls[1] as [string, RequestInit];
    expect(url).toBe("/api/buses/search");
    expect(init.headers).toMatchObject({ "X-XSRF-TOKEN": "csrf-from-body" });
    expect(init.credentials).toBe("include");
  });
});
