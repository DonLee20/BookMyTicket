import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { LogIn, ShieldCheck } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { useAuth } from "../context/AuthContext";
import { ApiError } from "../lib/api";

const schema = z.object({
  email: z.string().trim().email("Enter a valid email"),
  password: z.string().min(1, "Enter your password"),
});

type Values = z.infer<typeof schema>;

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const redirect = params.get("redirect") || "/bookings";
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<Values>({ resolver: zodResolver(schema), defaultValues: { email: "", password: "" } });

  const onSubmit = async (values: Values) => {
    setServerError(null);
    try {
      await login(values.email, values.password);
      navigate(redirect, { replace: true });
    } catch (err) {
      setServerError(err instanceof ApiError ? err.message : "We couldn’t sign you in. Please try again.");
    }
  };

  return (
    <div className="container container--narrow">
      <PageHead
        eyebrow="Welcome back"
        title="Sign in to your travel desk"
        lede="Session-based sign-in with an HttpOnly cookie. We never store tokens in your browser’s local storage."
        crumbs={[{ to: "/", label: "Home" }, { label: "Sign in" }]}
      />
      <div className="panel mb-4">
        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          {serverError && (
            <div className="notice notice--danger mb-2" role="alert">
              <LogIn size={17} aria-hidden="true" />
              <span>{serverError}</span>
            </div>
          )}
          <label className="field">
            <span className="field__label">Email</span>
            <input className="input" type="email" autoComplete="email" aria-invalid={Boolean(errors.email)} {...register("email")} />
            {errors.email && <span className="field__error">{errors.email.message}</span>}
          </label>
          <label className="field">
            <span className="field__label">Password</span>
            <input
              className="input"
              type="password"
              autoComplete="current-password"
              aria-invalid={Boolean(errors.password)}
              {...register("password")}
            />
            {errors.password && <span className="field__error">{errors.password.message}</span>}
          </label>
          <button type="submit" className="btn btn--primary btn--block mt-2" disabled={isSubmitting}>
            {isSubmitting ? "Signing in…" : "Sign in"}
          </button>
        </form>
        <p className="small muted center mt-2 mb-0">
          New here? <Link to="/register" className="link">Create an account</Link>
        </p>
        <div className="notice notice--info mt-3">
          <ShieldCheck size={17} aria-hidden="true" />
          <span className="small">
            Sign-in is required to create bookings and see your booking history. Browsing and search remain open to
            everyone.
          </span>
        </div>
      </div>
    </div>
  );
}
