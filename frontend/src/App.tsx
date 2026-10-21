import { Route, Routes } from "react-router-dom";
import { Layout } from "./components/Layout";
import { AdminRoute, ProtectedRoute } from "./components/ProtectedRoute";
import { HomePage } from "./pages/HomePage";
import { ExplorePage } from "./pages/ExplorePage";
import { BusPage } from "./pages/BusPage";
import { SeatSelectionPage } from "./pages/SeatSelectionPage";
import { PremiumPage } from "./pages/PremiumPage";
import { TrainPage } from "./pages/TrainPage";
import { FlightPage } from "./pages/FlightPage";
import { MtcPage } from "./pages/MtcPage";
import { HolidaysPage } from "./pages/HolidaysPage";
import { HolidayDetailPage } from "./pages/HolidayDetailPage";
import { BookingsPage } from "./pages/BookingsPage";
import { BookingDetailPage } from "./pages/BookingDetailPage";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { ProfilePage } from "./pages/ProfilePage";
import { AdminPage } from "./pages/AdminPage";
import { ContactPage } from "./pages/ContactPage";
import { NotFoundPage } from "./pages/NotFoundPage";

export function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="explore" element={<ExplorePage />} />
        <Route path="buses" element={<BusPage />} />
        <Route path="buses/:tripId/seats" element={<SeatSelectionPage />} />
        <Route path="premium" element={<PremiumPage />} />
        <Route path="trains" element={<TrainPage />} />
        <Route path="flights" element={<FlightPage />} />
        <Route path="chennai-mtc" element={<MtcPage />} />
        <Route path="holidays" element={<HolidaysPage />} />
        <Route path="holidays/:slug" element={<HolidayDetailPage />} />
        <Route
          path="bookings"
          element={
            <ProtectedRoute>
              <BookingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="bookings/:bookingId"
          element={
            <ProtectedRoute>
              <BookingDetailPage />
            </ProtectedRoute>
          }
        />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route
          path="profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin"
          element={
            <AdminRoute>
              <AdminPage />
            </AdminRoute>
          }
        />
        <Route path="contact" element={<ContactPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
