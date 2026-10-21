-- Railway, flight and Chennai MTC reference data structures
CREATE TABLE railway_stations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(80) NOT NULL,
    state VARCHAR(80) NOT NULL,
    zone VARCHAR(20) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_railway_stations_code (code),
    KEY idx_railway_stations_city (city)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE train_services (
    id BIGINT NOT NULL AUTO_INCREMENT,
    train_number VARCHAR(10) NOT NULL,
    name VARCHAR(150) NOT NULL,
    origin_station_id BIGINT NOT NULL,
    destination_station_id BIGINT NOT NULL,
    depart_minutes INT NOT NULL,
    arrive_minutes INT NOT NULL,
    duration_minutes INT NOT NULL,
    running_days VARCHAR(80) NOT NULL,
    total_distance_km INT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_train_services_number (train_number),
    KEY idx_train_services_route (origin_station_id, destination_station_id),
    CONSTRAINT fk_train_services_origin FOREIGN KEY (origin_station_id) REFERENCES railway_stations (id),
    CONSTRAINT fk_train_services_destination FOREIGN KEY (destination_station_id) REFERENCES railway_stations (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE train_service_classes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    service_id BIGINT NOT NULL,
    travel_class VARCHAR(30) NOT NULL,
    fare_minor BIGINT NULL,
    availability_status VARCHAR(40) NOT NULL DEFAULT 'SAMPLE',
    available_seats INT NULL,
    wl_position INT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_train_service_classes (service_id, travel_class),
    CONSTRAINT fk_train_service_classes_service FOREIGN KEY (service_id) REFERENCES train_services (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE train_route_stops (
    id BIGINT NOT NULL AUTO_INCREMENT,
    service_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    stop_number INT NOT NULL,
    arrive_offset_minutes INT NULL,
    depart_offset_minutes INT NULL,
    distance_km INT NULL,
    day_number INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_train_route_stops (service_id, stop_number),
    CONSTRAINT fk_train_route_stops_service FOREIGN KEY (service_id) REFERENCES train_services (id) ON DELETE CASCADE,
    CONSTRAINT fk_train_route_stops_station FOREIGN KEY (station_id) REFERENCES railway_stations (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE airports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    iata VARCHAR(3) NOT NULL,
    icao VARCHAR(4) NULL,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(80) NOT NULL,
    country VARCHAR(80) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    region VARCHAR(40) NOT NULL,
    timezone VARCHAR(60) NOT NULL,
    latitude DECIMAL(9, 6) NULL,
    longitude DECIMAL(9, 6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_airports_iata (iata),
    KEY idx_airports_city (city),
    KEY idx_airports_country (country_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE flight_schedules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    airline_code VARCHAR(4) NOT NULL,
    airline_name VARCHAR(120) NOT NULL,
    flight_number VARCHAR(10) NOT NULL,
    origin_airport_id BIGINT NOT NULL,
    destination_airport_id BIGINT NOT NULL,
    depart_minutes INT NOT NULL,
    duration_minutes INT NOT NULL,
    aircraft VARCHAR(40) NULL,
    stops INT NOT NULL DEFAULT 0,
    base_fare_economy_minor BIGINT NULL,
    base_fare_premium_minor BIGINT NULL,
    base_fare_business_minor BIGINT NULL,
    base_fare_first_minor BIGINT NULL,
    baggage_note VARCHAR(200) NULL,
    refundable TINYINT(1) NOT NULL DEFAULT 0,
    changeable TINYINT(1) NOT NULL DEFAULT 1,
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flight_schedules_number (airline_code, flight_number),
    KEY idx_flight_schedules_route (origin_airport_id, destination_airport_id),
    CONSTRAINT fk_flight_schedules_origin FOREIGN KEY (origin_airport_id) REFERENCES airports (id),
    CONSTRAINT fk_flight_schedules_destination FOREIGN KEY (destination_airport_id) REFERENCES airports (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE mtc_stops (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    locality VARCHAR(120) NOT NULL,
    zone VARCHAR(40) NULL,
    latitude DECIMAL(9, 6) NULL,
    longitude DECIMAL(9, 6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mtc_stops_name (name),
    KEY idx_mtc_stops_locality (locality)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE mtc_routes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    route_number VARCHAR(12) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    origin_stop_id BIGINT NOT NULL,
    destination_stop_id BIGINT NOT NULL,
    first_service VARCHAR(10) NOT NULL,
    last_service VARCHAR(10) NOT NULL,
    frequency_minutes INT NULL,
    service_days VARCHAR(60) NOT NULL DEFAULT 'DAILY',
    wheelchair_accessible TINYINT(1) NOT NULL DEFAULT 0,
    fare_min_minor BIGINT NOT NULL,
    fare_max_minor BIGINT NOT NULL,
    source VARCHAR(20) NOT NULL DEFAULT 'SAMPLE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mtc_routes_number_direction (route_number, direction),
    CONSTRAINT fk_mtc_routes_origin FOREIGN KEY (origin_stop_id) REFERENCES mtc_stops (id),
    CONSTRAINT fk_mtc_routes_destination FOREIGN KEY (destination_stop_id) REFERENCES mtc_stops (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE mtc_route_stops (
    id BIGINT NOT NULL AUTO_INCREMENT,
    route_id BIGINT NOT NULL,
    stop_id BIGINT NOT NULL,
    stop_sequence INT NOT NULL,
    stage_number INT NOT NULL,
    fare_from_origin_minor BIGINT NOT NULL,
    distance_km DECIMAL(6, 1) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mtc_route_stops (route_id, stop_sequence),
    CONSTRAINT fk_mtc_route_stops_route FOREIGN KEY (route_id) REFERENCES mtc_routes (id) ON DELETE CASCADE,
    CONSTRAINT fk_mtc_route_stops_stop FOREIGN KEY (stop_id) REFERENCES mtc_stops (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
