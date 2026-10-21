export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
  hasNext: boolean;
}

export interface ProviderMeta {
  providerMode: string;
  providerMessage?: string;
  sourceTimestamp?: string;
}

/* -------------------------------------------------------------------- buses */
export interface Amenity {
  code: string;
  label: string;
}

export interface BusTrip {
  id: number;
  operatorName: string;
  operatorCode: string;
  premium: boolean;
  busType: string;
  busTypeLabel: string;
  departureCity: string;
  arrivalCity: string;
  departTime: string;
  arriveTime: string;
  arriveDayOffset: number;
  arriveDayOffsetLabel: string;
  journeyDate: string;
  durationMinutes: number;
  durationLabel: string;
  fareMinor: number;
  fareDisplay: string;
  currency: string;
  availableSeats: number;
  totalSeats: number;
  availabilityStatus: string;
  amenities: Amenity[];
  boardingPoints: string[];
  droppingPoints: string[];
  cancellationPolicy: string;
  providerMode: string;
  source: string;
  seatSelectionSupported: boolean;
}

export type BusSearchStatus =
  | "OK"
  | "ORIGIN_NOT_COVERED"
  | "DESTINATION_NOT_COVERED"
  | "ROUTE_NOT_COVERED"
  | "OUTSIDE_DEMO_WINDOW"
  | "NO_TRIPS_ON_DATE"
  | "FILTERED_OUT";

export interface RouteSuggestion {
  from: string;
  to: string;
}

export interface BusSearchDiagnostics {
  status: BusSearchStatus;
  originCovered: boolean;
  destinationCovered: boolean;
  routeCovered: boolean;
  dateInInventoryWindow: boolean;
  inventoryFrom?: string;
  inventoryTo?: string;
  tripsOnDate: number;
  tripsAfterFilters: number;
  message: string;
  suggestions: RouteSuggestion[];
}

export interface BusSearchResponse extends ProviderMeta {
  trips: BusTrip[];
  liveDataConnected: boolean;
  liveProviderConfigured: boolean;
  diagnostics: BusSearchDiagnostics;
}

export interface PremiumHighlights extends ProviderMeta {
  trips: BusTrip[];
  comfortCategories: string[];
  liveDataConnected: boolean;
  liveProviderConfigured: boolean;
}

export interface SeatView {
  id: number;
  label: string;
  type: string;
  deck: string;
  rowNo: number;
  colNo: number;
  status: string;
  priceMinor: number;
  priceDisplay: string;
  window: boolean;
}

export interface SeatRow {
  rowNo: number;
  seats: SeatView[];
}

export interface DeckView {
  deck: string;
  deckLabel: string;
  rows: SeatRow[];
}

export interface SeatMap {
  tripId: number;
  layout: string;
  layoutLabel: string;
  pricePerSeatMinor: number;
  pricePerSeatDisplay: string;
  currency: string;
  availableCount: number;
  totalSeats: number;
  decks: DeckView[];
  providerMode: string;
}

export interface SeatMapResponse {
  trip: BusTrip;
  seatMap: SeatMap;
}

export interface BusSearchRequest {
  from: string;
  to: string;
  date: string;
  passengers: number;
  premiumOnly?: boolean;
  sort?: string;
  busTypes?: string[];
  amenities?: string[];
}

/* ------------------------------------------------------------------- trains */
export interface StationOption {
  code: string;
  name: string;
  city: string;
  state: string;
}

export interface ClassAvailability {
  code: string;
  label: string;
  fareMinor: number | null;
  fareDisplay: string | null;
  availabilityStatus: string;
  availableSeats: number | null;
  wlPosition: number | null;
  note: string | null;
}

export interface RouteStop {
  stationCode: string;
  stationName: string;
  arriveTime: string;
  departTime: string;
  dayNumber: number;
  distanceKm: number | null;
}

export interface TrainResult {
  id: number;
  trainNumber: string;
  name: string;
  fromCode: string;
  fromName: string;
  toCode: string;
  toName: string;
  departTime: string;
  arriveTime: string;
  arriveDayOffset: number;
  arriveDayOffsetLabel: string;
  durationMinutes: number;
  durationLabel: string;
  runningDays: string;
  runsOnDate: boolean;
  distanceKm: number | null;
  classes: ClassAvailability[];
  routeStops: RouteStop[];
}

export interface QuotaOption {
  code: string;
  label: string;
}

export interface TrainSearchResponse extends ProviderMeta {
  trains: TrainResult[];
  officialHandoffUrl: string;
  dataNote: string;
  quotas: QuotaOption[];
}

/* ------------------------------------------------------------------ flights */
export interface AirportOption {
  iata: string;
  name: string;
  city: string;
  country: string;
  region: string;
}

export interface FlightOffer {
  offerRef: string;
  scheduleId: number;
  airlineCode: string;
  airlineName: string;
  flightNumber: string;
  originIata: string;
  destinationIata: string;
  originCity: string;
  destinationCity: string;
  departLocal: string;
  arriveLocal: string;
  originTimezone: string;
  destinationTimezone: string;
  journeyDate: string;
  durationMinutes: number;
  stops: number;
  aircraft: string;
  cabin: string;
  cabinLabel: string;
  totalPriceMinor: number;
  pricePerTravellerMinor: number;
  currency: string;
  priceDisplay: string;
  baggageNote: string;
  refundable: boolean;
  changeable: boolean;
  fareConditions: string;
  seatsLeft: number | null;
  providerMode: string;
  sourceTimestamp: string;
  stale: boolean;
  externalHandoffUrl: string | null;
}

export interface FlightLegResult {
  index: number;
  originIata: string;
  destinationIata: string;
  originCity: string;
  destinationCity: string;
  date: string;
  offers: FlightOffer[];
}

export interface CabinOption {
  code: string;
  label: string;
}

export interface FlightSearchResponse extends ProviderMeta {
  legs: FlightLegResult[];
  ticketIssuanceAvailable: boolean;
  externalHandoffUrl: string | null;
  cabins: CabinOption[];
}

/* ---------------------------------------------------------------------- MTC */
export interface MtcStop {
  id: number;
  name: string;
  locality: string;
  zone: string;
}

export interface MtcRouteStop {
  stopId: number;
  name: string;
  sequence: number;
  stageNumber: number;
  fareFromOriginMinor: number;
  fareFromOriginDisplay: string;
  distanceKm: string;
  origin: boolean;
  destination: boolean;
}

export interface MtcRoutePlan {
  routeId: number;
  routeNumber: string;
  direction: string;
  directionLabel: string;
  originName: string;
  destinationName: string;
  firstService: string;
  lastService: string;
  frequencyMinutes: number | null;
  serviceDays: string;
  accessible: boolean;
  segmentFareMinor: number;
  segmentFareDisplay: string;
  travelStages: number;
  stops: MtcRouteStop[];
  source: string;
  dataNote: string;
}

export interface MtcTicketing {
  supported: boolean;
  mode: string;
  message: string;
  officialUrl: string;
}

export interface MtcPlanResponse {
  routes: MtcRoutePlan[];
  ticketing: MtcTicketing;
  providerMode: string;
  dataFreshness: string;
  message: string;
  sourceTimestamp: string;
  transferSuggestion: string | null;
}

/* ----------------------------------------------------------------- holidays */
export interface DestinationSummary {
  id: number;
  slug: string;
  name: string;
  region: string;
  country: string;
  kind: string;
  heroImageUrl: string;
  summary: string;
  bestSeason: string;
  featured: boolean;
}

export interface HolidayPackage {
  id: number;
  slug: string;
  title: string;
  summary: string;
  imageUrl: string;
  destinationName: string;
  destinationSlug: string;
  stateOrRegion: string;
  country: string;
  kind: string;
  category: string;
  categoryLabel: string;
  durationDays: number;
  durationNights: number;
  priceMinor: number;
  priceDisplay: string;
  currency: string;
  featured: boolean;
  landOnly: boolean;
  flightsIncluded: boolean;
  hotelsIncluded: boolean;
  transportIncluded: boolean;
  mealsIncluded: boolean;
}

export interface HolidayItineraryDay {
  dayNumber: number;
  title: string;
  description: string;
  meals: string;
}

export interface HolidayPackageDetail {
  summary: HolidayPackage;
  description: string;
  itinerary: HolidayItineraryDay[];
  inclusions: string[];
  exclusions: string[];
  highlights: string[];
  cancellationPolicy: string;
  inclusionNote: string;
}

export interface HolidaySearchResponse {
  packages: HolidayPackage[];
  categories: string[];
  destinations: DestinationSummary[];
  source: string;
}

export interface DestinationDetail {
  destination: DestinationSummary;
  description: string;
  packages: HolidayPackage[];
}

export interface TransportTeaser {
  category: string;
  title: string;
  description: string;
  searchPath: string;
}

export interface HolidayHomeResponse {
  featuredPackages: HolidayPackage[];
  featuredDestinations: DestinationSummary[];
  transportTeasers: TransportTeaser[];
}

/* ----------------------------------------------------------------- identity */
export interface User {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  roles: string[];
}

/* ----------------------------------------------------------------- bookings */
export interface PaymentSummary {
  id: string;
  provider: string;
  mode: string;
  status: string;
  amountMinor: number;
  amountDisplay: string;
  currency: string;
  providerOrderId: string;
  providerPaymentId: string | null;
  nextAction: string;
  message: string;
}

export interface PassengerView {
  seq: number;
  fullName: string;
  age: number;
  gender: string;
  passengerType: string;
  seatNumber: string | null;
}

export interface BookingDetails {
  bus: {
    tripId: number;
    operatorName: string;
    busType: string;
    premium: boolean;
    travelDate: string;
    departureCity: string;
    arrivalCity: string;
    departAt: string;
    arriveAt: string;
    boardingPoint: string;
    droppingPoint: string;
    seatNumbers: string[];
  } | null;
  train: {
    serviceId: number;
    trainNumber: string;
    trainName: string;
    travelClass: string;
    quota: string;
    journeyDate: string;
    fromStation: string;
    toStation: string;
    officialHandoffUrl: string;
  } | null;
  flight: {
    scheduleId: number;
    offerRef: string;
    airlineName: string;
    flightNumber: string;
    cabin: string;
    journeyDate: string;
    originIata: string;
    destinationIata: string;
    departLocal: string;
    arriveLocal: string;
    stops: number;
    baggageNote: string;
    refundable: boolean;
    changeable: boolean;
    providerConditions: string;
  } | null;
  holiday: {
    packageId: number;
    packageSlug: string;
    packageTitle: string;
    destinationName: string;
    imageUrl: string;
    startDate: string;
    endDate: string;
    travellers: number;
    rooms: number;
    landOnly: boolean;
  } | null;
}

export interface Booking {
  id: string;
  category: string;
  categoryLabel: string;
  status: string;
  statusLabel: string;
  paymentStatus: string;
  currency: string;
  totalAmountMinor: number;
  totalDisplay: string;
  providerMode: string;
  providerReference: string | null;
  contactName: string;
  contactEmail: string;
  contactPhone: string | null;
  travellerCount: number;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
  holdExpiresAt: string | null;
  holdSecondsRemaining: number | null;
  cancelledAt: string | null;
  cancellationReason: string | null;
  refundAmountMinor: number | null;
  refundDisplay: string | null;
  payment: PaymentSummary | null;
  details: BookingDetails;
  passengers: PassengerView[];
  nextAction: string;
  message: string;
  officialHandoffUrl: string | null;
}

export interface PassengerInput {
  fullName: string;
  age: number;
  gender: "MALE" | "FEMALE" | "OTHER";
  passengerType: "ADULT" | "CHILD" | "INFANT";
}

export interface CreateBusBooking {
  tripId: number;
  seatIds: number[];
  boardingPoint: string;
  droppingPoint: string;
  contactName: string;
  contactEmail: string;
  contactPhone: string;
  passengers: PassengerInput[];
  expectedTotalMinor: number;
  idempotencyKey: string;
}

export interface CreateTrainBooking {
  serviceId: number;
  travelClass: string;
  quota?: string;
  journeyDate: string;
  fromStation: string;
  toStation: string;
  contactName: string;
  contactEmail: string;
  contactPhone: string;
  passengers: PassengerInput[];
  idempotencyKey: string;
}

export interface CreateFlightBooking {
  offerRef: string;
  acceptPriceChange: boolean;
  contactName: string;
  contactEmail: string;
  contactPhone: string;
  passengers: PassengerInput[];
  idempotencyKey: string;
}

export interface CreateHolidayBooking {
  packageSlug: string;
  startDate: string;
  travellers: number;
  rooms: number;
  contactName: string;
  contactEmail: string;
  contactPhone: string;
  notes?: string;
  idempotencyKey: string;
}

export interface DemoCompletionResult {
  success: boolean;
  duplicate: boolean;
  bookingId: string;
  bookingStatus: string;
  paymentStatus: string;
  message: string;
}

/* -------------------------------------------------------------------- admin */
export interface AdminOverview {
  totalUsers: number;
  totalBookings: number;
  confirmedBookings: number;
  cancelledBookings: number;
  pendingPayments: number;
  revenueMinor: number;
  revenueDisplay: string;
  newMessages: number;
  totalPackages: number;
  activePackages: number;
  busTripsToday: number;
  busSeatsAvailableToday: number;
  bookingsByStatus: Record<string, number>;
  bookingsByCategory: Record<string, number>;
  paymentProviderMode: string;
  generatedAt: string;
}

export interface AdminUserRow {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  enabled: boolean;
  roles: string[];
  createdAt: string;
}

export interface AdminPaymentRow {
  id: string;
  bookingId: string;
  userEmail: string;
  provider: string;
  mode: string;
  status: string;
  amountMinor: number;
  amountDisplay: string;
  currency: string;
  providerOrderId: string;
  providerPaymentId: string | null;
  createdAt: string;
  failureReason: string | null;
}

export interface AdminPackageRow {
  id: number;
  slug: string;
  title: string;
  destinationName: string;
  category: string;
  categoryLabel: string;
  durationDays: number;
  basePriceMinor: number;
  priceDisplay: string;
  currency: string;
  active: boolean;
  featured: boolean;
  imageUrl: string;
}

export interface AdminBusInventoryRow {
  tripId: number;
  operatorName: string;
  busType: string;
  premium: boolean;
  direction: string;
  departureCity: string;
  arrivalCity: string;
  travelDate: string;
  departTime: string;
  arriveTime: string;
  fareMinor: number;
  fareDisplay: string;
  totalSeats: number;
  availableSeats: number;
  status: string;
  source: string;
}

export interface AdminMessageRow {
  id: number;
  name: string;
  email: string;
  subject: string;
  message: string;
  status: string;
  createdAt: string;
  handledAt: string | null;
}

export interface ProviderInfo {
  id: string;
  displayName: string;
  category: string;
  configuredMode: string;
  effectiveMode: string;
  message: string;
  bookingSupported: boolean;
  capabilities: string[];
}

export interface AdminHealth {
  status: string;
  database: string;
  schemaVersion: string;
  applicationVersion: string;
  uptimeSeconds: number;
  paymentProviderMode: string;
  providers: ProviderInfo[];
  maintenance: {
    totalUsers: number;
    totalBookings: number;
    totalPayments: number;
    totalProviderCalls: number;
    totalMessages: number;
  };
  serverTime: string;
}

/* ------------------------------------------------------------------ contact */
export interface ContactRequest {
  name: string;
  email: string;
  subject: string;
  message: string;
  company?: string;
}

export interface ContactResponse {
  id: number;
  status: string;
  message: string;
}
