-- Unified bookings, payments, provider transactions and contact messages
CREATE TABLE bookings (
    id VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    total_amount_minor BIGINT NOT NULL,
    provider_mode VARCHAR(20) NOT NULL,
    provider_reference VARCHAR(80) NULL,
    idempotency_key VARCHAR(80) NULL,
    contact_name VARCHAR(120) NOT NULL,
    contact_email VARCHAR(190) NOT NULL,
    contact_phone VARCHAR(30) NULL,
    traveller_count INT NOT NULL,
    hold_expires_at DATETIME(6) NULL,
    notes VARCHAR(500) NULL,
    cancelled_at DATETIME(6) NULL,
    cancellation_reason VARCHAR(300) NULL,
    refund_amount_minor BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bookings_idempotency (idempotency_key),
    KEY idx_bookings_user_created (user_id, created_at),
    KEY idx_bookings_status (status),
    KEY idx_bookings_hold (hold_expires_at),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE booking_passengers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id VARCHAR(36) NOT NULL,
    seq INT NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    passenger_type VARCHAR(20) NOT NULL DEFAULT 'ADULT',
    seat_number VARCHAR(10) NULL,
    seat_id BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_booking_passengers_seq (booking_id, seq),
    CONSTRAINT fk_booking_passengers_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE bus_booking_details (
    booking_id VARCHAR(36) NOT NULL,
    trip_id BIGINT NOT NULL,
    operator_name VARCHAR(150) NOT NULL,
    bus_type VARCHAR(40) NOT NULL,
    travel_date DATE NOT NULL,
    departure_city VARCHAR(80) NOT NULL,
    arrival_city VARCHAR(80) NOT NULL,
    depart_at DATETIME(6) NOT NULL,
    arrive_at DATETIME(6) NOT NULL,
    boarding_point VARCHAR(150) NULL,
    dropping_point VARCHAR(150) NULL,
    seat_numbers VARCHAR(200) NOT NULL,
    PRIMARY KEY (booking_id),
    CONSTRAINT fk_bus_booking_details_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_bus_booking_details_trip FOREIGN KEY (trip_id) REFERENCES bus_trips (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE train_booking_details (
    booking_id VARCHAR(36) NOT NULL,
    service_id BIGINT NOT NULL,
    travel_class VARCHAR(30) NOT NULL,
    quota VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    journey_date DATE NOT NULL,
    from_station VARCHAR(120) NOT NULL,
    to_station VARCHAR(120) NOT NULL,
    train_number VARCHAR(10) NOT NULL,
    train_name VARCHAR(150) NOT NULL,
    official_handoff_url VARCHAR(400) NOT NULL,
    PRIMARY KEY (booking_id),
    CONSTRAINT fk_train_booking_details_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_train_booking_details_service FOREIGN KEY (service_id) REFERENCES train_services (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE flight_booking_details (
    booking_id VARCHAR(36) NOT NULL,
    schedule_id BIGINT NOT NULL,
    offer_ref VARCHAR(80) NOT NULL,
    cabin VARCHAR(30) NOT NULL,
    journey_date DATE NOT NULL,
    origin_iata VARCHAR(3) NOT NULL,
    destination_iata VARCHAR(3) NOT NULL,
    airline_name VARCHAR(120) NOT NULL,
    flight_number VARCHAR(10) NOT NULL,
    depart_local VARCHAR(30) NOT NULL,
    arrive_local VARCHAR(30) NOT NULL,
    stops INT NOT NULL DEFAULT 0,
    baggage_note VARCHAR(200) NULL,
    refundable TINYINT(1) NOT NULL DEFAULT 0,
    changeable TINYINT(1) NOT NULL DEFAULT 1,
    provider_conditions VARCHAR(500) NULL,
    PRIMARY KEY (booking_id),
    CONSTRAINT fk_flight_booking_details_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_flight_booking_details_schedule FOREIGN KEY (schedule_id) REFERENCES flight_schedules (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE holiday_booking_details (
    booking_id VARCHAR(36) NOT NULL,
    package_id BIGINT NOT NULL,
    departure_id BIGINT NULL,
    package_title VARCHAR(200) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    travellers INT NOT NULL,
    rooms INT NOT NULL DEFAULT 1,
    land_only TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (booking_id),
    CONSTRAINT fk_holiday_booking_details_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_holiday_booking_details_package FOREIGN KEY (package_id) REFERENCES holiday_packages (id),
    CONSTRAINT fk_holiday_booking_details_departure FOREIGN KEY (departure_id) REFERENCES package_departures (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payments (
    id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    mode VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    provider_order_id VARCHAR(80) NULL,
    provider_payment_id VARCHAR(80) NULL,
    idempotency_key VARCHAR(120) NULL,
    failure_reason VARCHAR(300) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_order (provider_order_id),
    UNIQUE KEY uk_payments_idempotency (idempotency_key),
    KEY idx_payments_booking (booking_id),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payment_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    payment_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    event_ref VARCHAR(100) NOT NULL,
    payload_json TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_events_ref (payment_id, event_ref),
    CONSTRAINT fk_payment_events_payment FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE provider_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider VARCHAR(40) NOT NULL,
    category VARCHAR(20) NOT NULL,
    operation VARCHAR(40) NOT NULL,
    reference VARCHAR(100) NULL,
    success TINYINT(1) NOT NULL,
    latency_ms INT NULL,
    error_code VARCHAR(60) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_provider_transactions (provider, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE contact_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at DATETIME(6) NOT NULL,
    handled_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_contact_messages_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
