import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { UserPlus } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { useAuth } from "../context/AuthContext";
import { ApiError } from "../lib/api";

const schema = z.object({
  name: z.string().trim().min(2, "Enter your name").max(120),
  email: z.string().trim().email("Enter a valid email"),
  phone: z.string().regex(/^$|^[0-9+\- ]{6,20}$/, "Enter a valid phone number").optional(),
  password: z.string().min(8, "Use at least 8 characters").max(72),
});

type Values = z.infer<typeof schema>;

export function RegisterPage() {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const redirect = params.get("redirect") || "/bookings";
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { name: "", email: "", phone: "", password: "" },
  });

  const onSubmit = async (values: Values) => {
    setServerError(null);
    try {
      await registerUser({
        name: values.name,
        email: values.email,
        password: values.password,
        phone: values.phone || undefined,
      });
      navigate(redirect, { replace: true });
    } catch (err) {
      setServerError(err instanceof ApiError ? err.message : "We couldn’t create your account. Please try again.");
    }
  };

  return (
    <div className="container container--narrow">
      <PageHead
        eyebrow="Create an account"
        title="One account for every mode"
        lede="Your bookings, rail requests, flight requests and holiday packages stay together under a single profile."
        crumbs={[{ to: "/", label: "Home" }, { label: "Create account" }]}
      />
      <div className="panel mb-4">
        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          {serverError && (
            <div className="notice notice--danger mb-2" role="alert">
              <UserPlus size={17} aria-hidden="true" />
              <span>{serverError}</span>
            </div>
          )}
          <label className="field">
            <span className="field__label">Full name</span>
            <input className="input" autoComplete="name" {...register("name")} />
            {errors.name && <span className="field__error">{errors.name.message}</span>}
          </label>
          <label className="field">
            <span className="field__label">Email</span>
            <input className="input" type="email" autoComplete="email" {...register("email")} />
            {errors.email && <span className="field__error">{errors.email.message}</span>}
          </label>
          <label className="field">
            <span className="field__label">Phone (optional)</span>
            <input className="input" autoComplete="tel" {...register("phone")} />
            {errors.phone && <span className="field__error">{errors.phone.message}</span>}
          </label>
          <label className="field">
            <span className="field__label">Password</span>
            <input className="input" type="password" autoComplete="new-password" {...register("password")} />
            {errors.password && <span className="field__error">{errors.password.message}</span>}
            <span className="field__hint">At least 8 characters. Stored as a BCrypt hash, never in plain text.</span>
          </label>
          <button type="submit" className="btn btn--primary btn--block mt-2" disabled={isSubmitting}>
            {isSubmitting ? "Creating account…" : "Create account"}
          </button>
        </form>
        <p className="small muted center mt-2 mb-0">
          Already registered? <Link to="/login" className="link">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
