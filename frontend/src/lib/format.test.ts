import { describe, expect, it } from "vitest";
import { formatDuration, formatMoney, humanise, pluralise, statusTone } from "../lib/format";

describe("format helpers", () => {
  it("formats minor INR amounts as whole rupees", () => {
    expect(formatMoney(78000)).toContain("780");
    expect(formatMoney(1190000)).toContain("11,900");
  });

  it("formats durations in hours and minutes", () => {
    expect(formatDuration(450)).toBe("7h 30m");
    expect(formatDuration(60)).toBe("1h");
    expect(formatDuration(45)).toBe("45m");
  });

  it("humanises enum values for display", () => {
    expect(humanise("PAYMENT_PENDING")).toBe("Payment Pending");
    expect(humanise("AC_3_TIER")).toBe("Ac 3 Tier");
  });

  it("pluralises counts", () => {
    expect(pluralise(1, "traveller")).toBe("traveller");
    expect(pluralise(2, "traveller")).toBe("travellers");
  });

  it("maps statuses to visual tones", () => {
    expect(statusTone("CONFIRMED")).toBe("ok");
    expect(statusTone("PAYMENT_PENDING")).toBe("warn");
    expect(statusTone("CANCELLED")).toBe("danger");
    expect(statusTone("NOT_REQUIRED")).toBe("info");
    expect(statusTone("SOMETHING_ELSE")).toBe("neutral");
  });
});
