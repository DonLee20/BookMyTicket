import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { ProviderNotice } from "./Feedback";

describe("ProviderNotice", () => {
  it("presents demo bus results as sample data instead of a live-data failure", () => {
    render(
      <ProviderNotice
        mode="DEMO"
        message="Demo inventory: schedules, fares and seat availability are sample data."
        liveDataConnected={false}
        liveProviderConfigured={false}
      />,
    );

    expect(screen.getByText("Demo sample data")).toBeInTheDocument();
    expect(screen.getByText(/schedules, fares and seat availability are sample data/)).toBeInTheDocument();
    expect(screen.queryByText("Live data not connected")).not.toBeInTheDocument();
  });

  it("still warns when a non-demo provider has no live connection", () => {
    render(
      <ProviderNotice
        mode="EXTERNAL_HANDOFF"
        message="Search is available; booking continues on the provider channel."
        liveDataConnected={false}
      />,
    );

    expect(screen.getByText("Live data not connected")).toBeInTheDocument();
  });

  it("keeps other categories on the normal demo label", () => {
    render(<ProviderNotice mode="DEMO" message="Sample railway data." />);

    expect(screen.getByText("Demo sample data")).toBeInTheDocument();
    expect(screen.queryByText("Live data not connected")).not.toBeInTheDocument();
  });
});
