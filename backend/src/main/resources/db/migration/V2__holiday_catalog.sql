-- Tourism catalog: destinations, holiday packages, itinerary and departures
CREATE TABLE destinations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(80) NOT NULL,
    name VARCHAR(120) NOT NULL,
    region VARCHAR(120) NOT NULL,
    country VARCHAR(80) NOT NULL,
    kind VARCHAR(20) NOT NULL,
    hero_image_url VARCHAR(500) NULL,
    summary VARCHAR(400) NULL,
    description TEXT NULL,
    best_season VARCHAR(120) NULL,
    featured TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_destinations_slug (slug)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE holiday_packages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(120) NOT NULL,
    title VARCHAR(200) NOT NULL,
    destination_id BIGINT NOT NULL,
    category VARCHAR(40) NOT NULL,
    summary VARCHAR(400) NOT NULL,
    description TEXT NULL,
    duration_days INT NOT NULL,
    duration_nights INT NOT NULL,
    base_price_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    image_url VARCHAR(500) NULL,
    land_only TINYINT(1) NOT NULL DEFAULT 1,
    flights_included TINYINT(1) NOT NULL DEFAULT 0,
    hotels_included TINYINT(1) NOT NULL DEFAULT 1,
    transport_included TINYINT(1) NOT NULL DEFAULT 0,
    meals_included TINYINT(1) NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1,
    featured TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_holiday_packages_slug (slug),
    KEY idx_holiday_packages_destination (destination_id),
    CONSTRAINT fk_holiday_packages_destination FOREIGN KEY (destination_id) REFERENCES destinations (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE package_itinerary (
    id BIGINT NOT NULL AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    day_number INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    meals VARCHAR(120) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_package_itinerary_day (package_id, day_number),
    CONSTRAINT fk_package_itinerary_package FOREIGN KEY (package_id) REFERENCES holiday_packages (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE package_inclusions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    line VARCHAR(400) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_package_inclusions_package FOREIGN KEY (package_id) REFERENCES holiday_packages (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE package_exclusions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    line VARCHAR(400) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_package_exclusions_package FOREIGN KEY (package_id) REFERENCES holiday_packages (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE package_highlights (
    id BIGINT NOT NULL AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    line VARCHAR(400) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_package_highlights_package FOREIGN KEY (package_id) REFERENCES holiday_packages (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE package_departures (
    id BIGINT NOT NULL AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    departure_date DATE NOT NULL,
    capacity INT NOT NULL,
    seats_booked INT NOT NULL DEFAULT 0,
    price_override_minor BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_package_departures (package_id, departure_date),
    CONSTRAINT fk_package_departures_package FOREIGN KEY (package_id) REFERENCES holiday_packages (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
