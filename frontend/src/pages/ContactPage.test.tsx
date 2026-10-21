import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { ToastProvider } from "../context/ToastContext";
import { ContactPage } from "../pages/ContactPage";

describe("contact form", () => {
  it("shows validation messages instead of submitting an empty form", async () => {
    const { default: userEvent } = await import("@testing-library/user-event");
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <ToastProvider>
          <ContactPage />
        </ToastProvider>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: /send message/i }));

    expect(await screen.findByText("Enter your name")).toBeInTheDocument();
    expect(screen.getByText("Enter a valid email")).toBeInTheDocument();
    expect(screen.getByText("Add a short subject")).toBeInTheDocument();
    expect(screen.getByText("Tell us a little more")).toBeInTheDocument();
  });
});
