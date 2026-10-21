import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { ProviderNotice } from "./Feedback";

describe("ProviderNotice", () => {
  it("marks demo bus results as not connected to live data and shows the backend setup path", () => {
    render(
      <ProviderNotice
        mode="DEMO"
        message="Demo inventory: schedules, fares and seat availability are sample data."
        liveDataConnected={false}
        liveProviderConfigured={false}
      />,
    );

    expect(screen.getByText("Live bus data not connected")).toBeInTheDocument();
    expect(screen.getByText(/BUS_PROVIDER_MODE=LIVE/)).toBeInTheDocument();
    expect(screen.getByText(/Never expose credentials to React/)).toBeInTheDocument();
  });

  it("keeps other categories on the normal demo label", () => {
    render(<ProviderNotice mode="DEMO" message="Sample railway data." />);

    expect(screen.getByText("Demo inventory")).toBeInTheDocument();
    expect(screen.queryByText("Live bus data not connected")).not.toBeInTheDocument();
  });
});
