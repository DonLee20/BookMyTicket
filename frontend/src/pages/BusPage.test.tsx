import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { BusPage } from "./BusPage";
import { api } from "../lib/api";

vi.mock("../lib/api", () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe("bus search diagnostics", () => {
  beforeEach(() => {
    vi.mocked(api.get).mockResolvedValue({
      cities: ["Hyderabad", "Digha", "Kolkata"],
      origin: true,
    } as never);
  });

  it("shows route-not-covered guidance instead of pretending the date is empty", async () => {
    vi.mocked(api.post).mockResolvedValue({
      trips: [],
      providerMode: "DEMO",
      providerMessage: "Demo inventory: schedules, fares and seat availability are sample data.",
      sourceTimestamp: "2026-10-20T06:10:26Z",
      liveDataConnected: false,
      liveProviderConfigured: false,
      diagnostics: {
        status: "ROUTE_NOT_COVERED",
        originCovered: true,
        destinationCovered: true,
        routeCovered: false,
        dateInInventoryWindow: true,
        inventoryFrom: "2026-10-20",
        inventoryTo: "2026-11-19",
        tripsOnDate: 0,
        tripsAfterFilters: 0,
        message: "Demo inventory covers Hyderabad and Digha separately, but there is no direct route between them.",
        suggestions: [{ from: "Kolkata", to: "Digha" }],
      },
    } as never);

    render(
      <MemoryRouter initialEntries={["/buses?from=Hyderabad&to=Digha&date=2026-10-21&passengers=1"]}>
        <BusPage />
      </MemoryRouter>,
    );

    expect(await screen.findByText("Demo sample data")).toBeInTheDocument();
    expect(await screen.findByText("No demo route for this pair")).toBeInTheDocument();
    expect(await screen.findByText("Kolkata → Digha")).toBeInTheDocument();
    expect(screen.queryByText("No sample coaches on this date")).not.toBeInTheDocument();
  });
});
