import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Mail, MapPin, MessageSquare } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { useToast } from "../context/ToastContext";
import { api, ApiError } from "../lib/api";
import type { ContactResponse } from "../lib/types";

const schema = z.object({
  name: z.string().trim().min(2, "Enter your name").max(120),
  email: z.string().trim().email("Enter a valid email"),
  subject: z.string().trim().min(3, "Add a short subject").max(150),
  message: z.string().trim().min(10, "Tell us a little more").max(2000),
  company: z.string().max(0).optional(),
});

type Values = z.infer<typeof schema>;

export function ContactPage() {
  const { notify } = useToast();
  const [success, setSuccess] = useState<string | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { name: "", email: "", subject: "", message: "", company: "" },
  });

  const onSubmit = async (values: Values) => {
    setServerError(null);
    setSuccess(null);
    try {
      const response = await api.post<ContactResponse>("/api/contact", values);
      setSuccess(response.message);
      notify("Message received. The team will follow up by email.", "ok");
      reset();
    } catch (err) {
      setServerError(err instanceof ApiError ? err.message : "We couldn’t send your message. Please try again.");
    }
  };

  return (
    <div className="container container--narrow">
      <PageHead
        eyebrow="Contact"
        title="Questions, corrections, or a route we’ve missed"
        lede="Tell us what you were trying to do. Messages are stored in the platform inbox and visible to the operations team."
        crumbs={[{ to: "/", label: "Home" }, { label: "Contact" }]}
      />

      <div className="grid grid--2 mb-4">
        <div className="card card--pad">
          <h2 className="row" style={{ gap: "0.5rem" }}>
            <MessageSquare size={19} aria-hidden="true" /> Send a message
          </h2>
          {success && (
            <div className="notice notice--ok mb-2" role="status">
              <Mail size={17} aria-hidden="true" />
              <span>{success}</span>
            </div>
          )}
          {serverError && (
            <div className="notice notice--danger mb-2" role="alert">
              <Mail size={17} aria-hidden="true" />
              <span>{serverError}</span>
            </div>
          )}
          <form onSubmit={handleSubmit(onSubmit)} noValidate>
            <label className="field">
              <span className="field__label">Name</span>
              <input className="input" autoComplete="name" {...register("name")} />
              {errors.name && <span className="field__error">{errors.name.message}</span>}
            </label>
            <label className="field">
              <span className="field__label">Email</span>
              <input className="input" type="email" autoComplete="email" {...register("email")} />
              {errors.email && <span className="field__error">{errors.email.message}</span>}
            </label>
            <label className="field">
              <span className="field__label">Subject</span>
              <input className="input" {...register("subject")} />
              {errors.subject && <span className="field__error">{errors.subject.message}</span>}
            </label>
            <label className="field">
              <span className="field__label">Message</span>
              <textarea className="textarea" rows={6} maxLength={2000} {...register("message")} />
              {errors.message && <span className="field__error">{errors.message.message}</span>}
            </label>
            <div className="visually-hidden" aria-hidden="true">
              <label>
                Company
                <input tabIndex={-1} autoComplete="off" {...register("company")} />
              </label>
            </div>
            <button type="submit" className="btn btn--primary mt-2" disabled={isSubmitting}>
              {isSubmitting ? "Sending…" : "Send message"}
            </button>
          </form>
        </div>

        <div>
          <div className="card card--pad">
            <h2>How this desk works</h2>
            <p className="small muted">
              This is a demonstration booking platform. We can answer questions about the sample inventory, the booking
              lifecycle, provider boundaries and the API. We cannot issue real bus, rail or flight tickets.
            </p>
            <ul className="list-checks">
              <li>
                <Mail size={15} aria-hidden="true" /> Messages are stored in the platform database.
              </li>
              <li>
                <Mail size={15} aria-hidden="true" /> We reply by email; no marketing lists.
              </li>
              <li>
                <MapPin size={15} aria-hidden="true" /> Chennai, India · records stored in UTC.
              </li>
            </ul>
          </div>
          <div className="notice notice--info mt-2">
            <CompassNote />
          </div>
        </div>
      </div>
    </div>
  );
}

function CompassNote() {
  return (
    <span className="small">
      For real travel, use an authorised provider. Rail tickets are issued by IRCTC; Chennai MTC tickets by the official
      MTC portal; flights by the airline or your travel agent.
    </span>
  );
}
