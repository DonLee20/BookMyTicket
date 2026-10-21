-- Intercity and premium bus domain
CREATE TABLE bus_operators (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(20) NOT NULL,
    premium TINYINT(1) NOT NULL DEFAULT 0,
    description VARCHAR(400) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bus_operators_name (name),
    UNIQUE KEY uk_bus_operators_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE bus_corridors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    city_a VARCHAR(80) NOT NULL,
    city_b VARCHAR(80) NOT NULL,
    distance_km INT NOT NULL,
    duration_minutes INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bus_corridors_pair (city_a, city_b),
    KEY idx_bus_corridors_city_a (city_a),
    KEY idx_bus_corridors_city_b (city_b)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE bus_schedules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    corridor_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    depart_minutes INT NOT NULL,
    duration_minutes INT NOT NULL,
    fare_minor BIGINT NOT NULL,
    bus_type VARCHAR(40) NOT NULL,
    premium TINYINT(1) NOT NULL DEFAULT 0,
    layout VARCHAR(40) NOT NULL,
    amenities VARCHAR(500) NULL,
    boarding_points VARCHAR(500) NULL,
    dropping_points VARCHAR(500) NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    KEY idx_bus_schedules_corridor (corridor_id),
    CONSTRAINT fk_bus_schedules_corridor FOREIGN KEY (corridor_id) REFERENCES bus_corridors (id),
    CONSTRAINT fk_bus_schedules_operator FOREIGN KEY (operator_id) REFERENCES bus_operators (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE bus_trips (
    id BIGINT NOT NULL AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL,
    direction VARCHAR(12) NOT NULL,
    travel_date DATE NOT NULL,
    departure_city VARCHAR(80) NOT NULL,
    arrival_city VARCHAR(80) NOT NULL,
    depart_at DATETIME(6) NOT NULL,
    arrive_at DATETIME(6) NOT NULL,
    fare_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    bus_type VARCHAR(40) NOT NULL,
    premium TINYINT(1) NOT NULL DEFAULT 0,
    layout VARCHAR(40) NOT NULL,
    amenities VARCHAR(500) NULL,
    boarding_points VARCHAR(500) NULL,
    dropping_points VARCHAR(500) NULL,
    total_seats INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    source VARCHAR(20) NOT NULL DEFAULT 'DEMO',
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bus_trips_schedule_date_dir (schedule_id, travel_date, direction),
    KEY idx_bus_trips_search (departure_city, arrival_city, travel_date),
    CONSTRAINT fk_bus_trips_schedule FOREIGN KEY (schedule_id) REFERENCES bus_schedules (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE bus_seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trip_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    seat_type VARCHAR(20) NOT NULL,
    deck VARCHAR(10) NOT NULL,
    row_no INT NOT NULL,
    col_no INT NOT NULL,
    price_minor BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bus_seats_trip_number (trip_id, seat_number),
    KEY idx_bus_seats_trip_status (trip_id, status),
    CONSTRAINT fk_bus_seats_trip FOREIGN KEY (trip_id) REFERENCES bus_trips (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
