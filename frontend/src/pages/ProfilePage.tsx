import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { KeyRound, LogOut, UserRound } from "lucide-react";
import { PageHead } from "../components/PageHead";
import { ErrorState, LoadingBlock } from "../components/Feedback";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { api, ApiError } from "../lib/api";
import { useAsync } from "../hooks/useAsync";

interface ProfileResponse {
  id: number;
  name: string;
  email: string;
  phone: string | null;
}

const profileSchema = z.object({
  name: z.string().trim().min(2, "Enter your name").max(120),
  phone: z.string().regex(/^$|^[0-9+\- ]{6,20}$/, "Enter a valid phone number").or(z.literal("")),
});

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, "Enter your current password"),
    newPassword: z.string().min(8, "Use at least 8 characters").max(72),
    confirmPassword: z.string().min(1, "Confirm the new password"),
  })
  .refine((value) => value.newPassword === value.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });

type ProfileValues = z.infer<typeof profileSchema>;
type PasswordValues = z.infer<typeof passwordSchema>;

export function ProfilePage() {
  const { user, logout, setUser } = useAuth();
  const { notify } = useToast();
  const { data, loading, error, reload } = useAsync<ProfileResponse>(
    (signal) => api.get("/api/profile", signal),
    [],
  );

  const profileForm = useForm<ProfileValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: { name: user?.name ?? "", phone: user?.phone ?? "" },
  });
  const passwordForm = useForm<PasswordValues>({
    resolver: zodResolver(passwordSchema),
    defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
  });
  const [savingPassword, setSavingPassword] = useState(false);

  useEffect(() => {
    if (data) {
      profileForm.reset({ name: data.name, phone: data.phone ?? "" });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data?.id, data?.name, data?.phone]);

  const saveProfile = async (values: ProfileValues) => {
    try {
      const response = await api.put<ProfileResponse>("/api/profile", {
        name: values.name,
        phone: values.phone,
      });
      setUser(user ? { ...user, name: response.name, phone: response.phone } : user);
      notify("Profile updated.", "ok");
    } catch (err) {
      notify(err instanceof ApiError ? err.message : "We couldn’t update your profile.", "error");
    }
  };

  const savePassword = async (values: PasswordValues) => {
    setSavingPassword(true);
    try {
      await api.post("/api/profile/password", {
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      passwordForm.reset({ currentPassword: "", newPassword: "", confirmPassword: "" });
      notify("Password updated.", "ok");
    } catch (err) {
      notify(err instanceof ApiError ? err.message : "We couldn’t change your password.", "error");
    } finally {
      setSavingPassword(false);
    }
  };

  return (
    <div className="container container--narrow">
      <PageHead
        eyebrow="Account"
        title="Your profile"
        lede="Keep your contact details current so booking confirmations reach the right inbox."
        crumbs={[{ to: "/", label: "Home" }, { label: "Profile" }]}
        actions={
          <>
            <Link to="/bookings" className="btn btn--ghost btn--sm">
              My bookings
            </Link>
            <button
              type="button"
              className="btn btn--ghost btn--sm"
              onClick={() => {
                void logout();
              }}
            >
              <LogOut size={15} aria-hidden="true" /> Sign out
            </button>
          </>
        }
      />

      {loading && <LoadingBlock label="Loading your profile" />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}

      {data && (
        <div className="grid grid--2 mb-4">
          <div className="card card--pad">
            <h2 className="row" style={{ gap: "0.5rem" }}>
              <UserRound size={19} aria-hidden="true" /> Profile details
            </h2>
            <form onSubmit={profileForm.handleSubmit(saveProfile)} noValidate>
              <label className="field">
                <span className="field__label">Full name</span>
                <input className="input" {...profileForm.register("name")} />
                {profileForm.formState.errors.name && (
                  <span className="field__error">{profileForm.formState.errors.name.message}</span>
                )}
              </label>
              <label className="field">
                <span className="field__label">Email</span>
                <input className="input" value={data.email} readOnly />
                <span className="field__hint">Email changes are not supported in this release.</span>
              </label>
              <label className="field">
                <span className="field__label">Phone</span>
                <input className="input" {...profileForm.register("phone")} />
                {profileForm.formState.errors.phone && (
                  <span className="field__error">{profileForm.formState.errors.phone.message}</span>
                )}
              </label>
              <button type="submit" className="btn btn--primary mt-2" disabled={profileForm.formState.isSubmitting}>
                Save changes
              </button>
            </form>
          </div>

          <div className="card card--pad">
            <h2 className="row" style={{ gap: "0.5rem" }}>
              <KeyRound size={19} aria-hidden="true" /> Change password
            </h2>
            <form onSubmit={passwordForm.handleSubmit(savePassword)} noValidate>
              <label className="field">
                <span className="field__label">Current password</span>
                <input className="input" type="password" autoComplete="current-password" {...passwordForm.register("currentPassword")} />
                {passwordForm.formState.errors.currentPassword && (
                  <span className="field__error">{passwordForm.formState.errors.currentPassword.message}</span>
                )}
              </label>
              <label className="field">
                <span className="field__label">New password</span>
                <input className="input" type="password" autoComplete="new-password" {...passwordForm.register("newPassword")} />
                {passwordForm.formState.errors.newPassword && (
                  <span className="field__error">{passwordForm.formState.errors.newPassword.message}</span>
                )}
              </label>
              <label className="field">
                <span className="field__label">Confirm new password</span>
                <input className="input" type="password" autoComplete="new-password" {...passwordForm.register("confirmPassword")} />
                {passwordForm.formState.errors.confirmPassword && (
                  <span className="field__error">{passwordForm.formState.errors.confirmPassword.message}</span>
                )}
              </label>
              <button type="submit" className="btn btn--primary mt-2" disabled={savingPassword}>
                {savingPassword ? "Updating…" : "Update password"}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
