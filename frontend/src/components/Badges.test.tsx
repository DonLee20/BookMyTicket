import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { StatusBadge } from "../components/Badges";
import { DemoBadge } from "../components/Badges";

describe("status badges", () => {
  it("renders the friendly status label and tone class", () => {
    render(<StatusBadge status="CONFIRMED" label="Confirmed" />);
    const badge = screen.getByText("Confirmed");
    expect(badge).toHaveClass("badge", "badge--ok");
  });

  it("renders a demo badge only when a mode is supplied", () => {
    render(<DemoBadge mode="DEMO" />);
    expect(screen.getByText("Demo data")).toHaveClass("badge--warn");
  });
});
