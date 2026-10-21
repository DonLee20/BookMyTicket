-- Chennai MTC demonstration data. Stop names, routes, timings and fares here
-- are SAMPLE data for the route-planning experience (source = SAMPLE). They are
-- not live operational data. MTC ticketing is not enabled.
INSERT INTO mtc_stops (name, locality, zone, latitude, longitude)
VALUES ('Broadway', 'George Town', 'North', 13.091900, 80.287400),
       ('Chennai Central', 'Periyamet', 'North', 13.083500, 80.275500),
       ('Egmore', 'Egmore', 'Central', 13.078500, 80.260600),
       ('T. Nagar', 'T. Nagar', 'Central', 13.041800, 80.233700),
       ('Guindy', 'Guindy', 'South', 13.006700, 80.220600),
       ('Tambaram', 'Tambaram', 'South', 12.922900, 80.127500),
       ('CMBT Koyambedu', 'Koyambedu', 'West', 13.070700, 80.194400),
       ('Madhavaram', 'Madhavaram', 'North', 13.148600, 80.231000),
       ('Perambur', 'Perambur', 'North', 13.108900, 80.232900),
       ('Adyar', 'Adyar', 'South', 13.001200, 80.256500),
       ('Thiruvanmiyur', 'Thiruvanmiyur', 'South', 12.983000, 80.259500),
       ('Velachery', 'Velachery', 'South', 12.975600, 80.220700),
       ('Porur', 'Porur', 'West', 13.035900, 80.156500),
       ('Poonamallee', 'Poonamallee', 'West', 13.048900, 80.094900),
       ('Ambattur', 'Ambattur', 'West', 13.114300, 80.154800),
       ('Avadi', 'Avadi', 'North West', 13.114700, 80.109800),
       ('Anna Nagar', 'Anna Nagar', 'Central', 13.085500, 80.210100),
       ('Kilpauk', 'Kilpauk', 'Central', 13.078600, 80.242500),
       ('Saidapet', 'Saidapet', 'Central', 13.021300, 80.223100),
       ('Mylapore', 'Mylapore', 'Central', 13.033900, 80.269600),
       ('Besant Nagar', 'Besant Nagar', 'South', 13.000200, 80.266800),
       ('Chromepet', 'Chromepet', 'South', 12.951600, 80.141600),
       ('Pallavaram', 'Pallavaram', 'South', 12.967500, 80.149500),
       ('Sholinganallur', 'Sholinganallur', 'South', 12.901000, 80.227900),
       ('Kelambakkam', 'Kelambakkam', 'South', 12.787500, 80.220900),
       ('Red Hills', 'Red Hills', 'North West', 13.190500, 80.189100),
       ('Ennore', 'Ennore', 'North', 13.218900, 80.321000),
       ('Thiruvottiyur', 'Thiruvottiyur', 'North', 13.166700, 80.303600),
       ('Vadapalani', 'Vadapalani', 'Central', 13.050400, 80.212300),
       ('Vandalur', 'Vandalur', 'South', 12.870900, 80.080500),
       ('Alandur', 'Alandur', 'South', 13.002500, 80.201600);

-- ---------------------------------------------------------------- route 21G
SET @r := NULL;
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('21G', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Broadway'), (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), '04:45', '22:30', 10, 'DAILY', 1, 500, 2200);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Broadway'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 2, 1, 500, 2),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 3, 2, 700, 4),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Saidapet'), 4, 3, 1000, 10),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Guindy'), 5, 4, 1200, 14),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Pallavaram'), 6, 5, 1600, 22),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chromepet'), 7, 5, 1800, 25),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), 8, 6, 2200, 30);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('21G', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), (SELECT id FROM mtc_stops WHERE name = 'Broadway'), '04:30', '22:15', 10, 'DAILY', 1, 500, 2200);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chromepet'), 2, 1, 500, 5),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Pallavaram'), 3, 2, 700, 8),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Guindy'), 4, 3, 1200, 16),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Saidapet'), 5, 4, 1400, 20),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 6, 5, 1800, 26),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 7, 5, 2000, 28),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Broadway'), 8, 6, 2200, 30);

-- ---------------------------------------------------------------- route 5E
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('5E', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Broadway'), (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), '05:00', '22:00', 8, 'DAILY', 1, 500, 1400);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Broadway'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 2, 1, 500, 2),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 3, 2, 700, 3),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Kilpauk'), 4, 3, 900, 6),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Anna Nagar'), 5, 4, 1100, 9),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), 6, 5, 1400, 12);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('5E', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), (SELECT id FROM mtc_stops WHERE name = 'Broadway'), '05:00', '22:00', 8, 'DAILY', 1, 500, 1400);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Anna Nagar'), 2, 1, 500, 3),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Kilpauk'), 3, 2, 700, 6),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 4, 3, 900, 9),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 5, 4, 1100, 11),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Broadway'), 6, 5, 1400, 12);

-- ---------------------------------------------------------------- route 51A
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('51A', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), (SELECT id FROM mtc_stops WHERE name = 'Velachery'), '04:50', '22:20', 12, 'DAILY', 1, 500, 1800);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Mylapore'), 2, 2, 700, 5),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Adyar'), 3, 3, 1100, 10),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Thiruvanmiyur'), 4, 4, 1400, 13),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Velachery'), 5, 5, 1800, 17);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('51A', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'Velachery'), (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), '04:40', '22:10', 12, 'DAILY', 1, 500, 1800);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Velachery'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Thiruvanmiyur'), 2, 1, 500, 4),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Adyar'), 3, 2, 900, 7),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Mylapore'), 4, 3, 1300, 12),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 5, 5, 1800, 17);

-- ---------------------------------------------------------------- route 29C
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('29C', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Egmore'), (SELECT id FROM mtc_stops WHERE name = 'Thiruvanmiyur'), '05:10', '21:50', 15, 'DAILY', 0, 500, 1600);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Mylapore'), 2, 2, 700, 4),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Adyar'), 3, 3, 1100, 9),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Besant Nagar'), 4, 4, 1300, 12),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Thiruvanmiyur'), 5, 4, 1600, 14);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('29C', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'Thiruvanmiyur'), (SELECT id FROM mtc_stops WHERE name = 'Egmore'), '05:10', '21:50', 15, 'DAILY', 0, 500, 1600);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Thiruvanmiyur'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Besant Nagar'), 2, 1, 500, 2),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Adyar'), 3, 2, 900, 5),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Mylapore'), 4, 3, 1300, 10),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 5, 4, 1600, 14);

-- ---------------------------------------------------------------- route 12C
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('12C', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), (SELECT id FROM mtc_stops WHERE name = 'Poonamallee'), '05:00', '21:40', 20, 'DAILY', 1, 500, 1900);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 2, 1, 500, 3),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Vadapalani'), 3, 2, 900, 7),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Porur'), 4, 4, 1400, 12),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Poonamallee'), 5, 5, 1900, 17);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('12C', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'Poonamallee'), (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), '04:50', '21:30', 20, 'DAILY', 1, 500, 1900);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Poonamallee'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Porur'), 2, 2, 700, 5),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Vadapalani'), 3, 4, 1300, 10),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 4, 5, 1700, 14),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 5, 5, 1900, 17);

-- ---------------------------------------------------------------- route 40A
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('40A', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Anna Nagar'), (SELECT id FROM mtc_stops WHERE name = 'Besant Nagar'), '05:15', '21:45', 12, 'DAILY', 1, 500, 1700);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Anna Nagar'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Kilpauk'), 2, 1, 500, 3),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 3, 2, 900, 6),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Mylapore'), 4, 3, 1300, 10),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Besant Nagar'), 5, 5, 1700, 14);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('40A', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'Besant Nagar'), (SELECT id FROM mtc_stops WHERE name = 'Anna Nagar'), '05:15', '21:45', 12, 'DAILY', 1, 500, 1700);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Besant Nagar'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Mylapore'), 2, 2, 700, 4),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 3, 3, 1300, 8),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Kilpauk'), 4, 5, 1500, 11),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Anna Nagar'), 5, 5, 1700, 14);

-- ---------------------------------------------------------------- route 78
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('78', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Broadway'), (SELECT id FROM mtc_stops WHERE name = 'Ennore'), '05:20', '21:20', 25, 'DAILY', 0, 500, 1600);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Broadway'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Thiruvottiyur'), 2, 3, 1100, 12),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Ennore'), 3, 5, 1600, 18);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('78', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'Ennore'), (SELECT id FROM mtc_stops WHERE name = 'Broadway'), '05:20', '21:20', 25, 'DAILY', 0, 500, 1600);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Ennore'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Thiruvottiyur'), 2, 2, 700, 6),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Broadway'), 3, 5, 1600, 18);

-- ---------------------------------------------------------------- route 70
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('70', 'UP', (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), (SELECT id FROM mtc_stops WHERE name = 'Avadi'), '05:00', '22:00', 10, 'DAILY', 1, 500, 1500);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Ambattur'), 2, 2, 800, 7),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Avadi'), 3, 4, 1500, 13);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('70', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'Avadi'), (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), '05:00', '22:00', 10, 'DAILY', 1, 500, 1500);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Avadi'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Ambattur'), 2, 2, 800, 6),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), 3, 4, 1500, 13);

-- ---------------------------------------------------------------- route 570 (OMR)
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('570', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), (SELECT id FROM mtc_stops WHERE name = 'Sholinganallur'), '05:30', '21:30', 20, 'DAILY', 1, 500, 1900);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chromepet'), 2, 1, 500, 4),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Pallavaram'), 3, 2, 700, 7),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Velachery'), 4, 3, 1200, 13),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Sholinganallur'), 5, 5, 1900, 22);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('570', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'Sholinganallur'), (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), '05:30', '21:30', 20, 'DAILY', 1, 500, 1900);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Sholinganallur'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Velachery'), 2, 3, 1200, 9),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Pallavaram'), 3, 4, 1500, 15),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chromepet'), 4, 5, 1700, 18),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), 5, 5, 1900, 22);

-- ---------------------------------------------------------------- route 109 (OMR / Kelambakkam)
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('109', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), (SELECT id FROM mtc_stops WHERE name = 'Kelambakkam'), '05:00', '21:00', 30, 'DAILY', 1, 500, 3000);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 2, 1, 500, 3),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Adyar'), 3, 3, 1200, 10),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Thiruvanmiyur'), 4, 4, 1500, 13),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Sholinganallur'), 5, 6, 2200, 22),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Kelambakkam'), 6, 8, 3000, 32);
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('109', 'DOWN', (SELECT id FROM mtc_stops WHERE name = 'Kelambakkam'), (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), '05:00', '21:00', 30, 'DAILY', 1, 500, 3000);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Kelambakkam'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Sholinganallur'), 2, 3, 1200, 10),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Thiruvanmiyur'), 3, 5, 1800, 19),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Adyar'), 4, 6, 2100, 22),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 5, 8, 2700, 29),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 6, 8, 3000, 32);

-- ---------------------------------------------------------------- route 17M
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('17M', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Broadway'), (SELECT id FROM mtc_stops WHERE name = 'Vadapalani'), '05:10', '22:10', 12, 'DAILY', 1, 500, 1300);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Broadway'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 2, 1, 500, 2),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 3, 2, 700, 4),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Saidapet'), 4, 3, 1000, 8),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Vadapalani'), 5, 4, 1300, 11);

-- ---------------------------------------------------------------- route 27B
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('27B', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), (SELECT id FROM mtc_stops WHERE name = 'Mylapore'), '05:25', '21:55', 10, 'DAILY', 1, 500, 900);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Chennai Central'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 2, 1, 500, 3),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Mylapore'), 3, 2, 900, 6);

-- ---------------------------------------------------------------- route 47A
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('47A', 'UP', (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), (SELECT id FROM mtc_stops WHERE name = 'Red Hills'), '05:15', '21:15', 25, 'DAILY', 0, 500, 1500);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Madhavaram'), 2, 3, 1100, 9),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Red Hills'), 3, 4, 1500, 13);

-- ---------------------------------------------------------------- route 15B
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('15B', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Broadway'), (SELECT id FROM mtc_stops WHERE name = 'Thiruvottiyur'), '05:05', '22:05', 10, 'DAILY', 1, 500, 1400);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Broadway'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Perambur'), 2, 2, 800, 7),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Thiruvottiyur'), 3, 4, 1400, 12);

-- ---------------------------------------------------------------- route 5 (long north-south)
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('5', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), (SELECT id FROM mtc_stops WHERE name = 'Madhavaram'), '04:40', '21:00', 20, 'DAILY', 0, 500, 2700);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chromepet'), 2, 1, 500, 4),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Guindy'), 3, 3, 1200, 12),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Egmore'), 4, 6, 2100, 24),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'CMBT Koyambedu'), 5, 8, 2500, 30),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Madhavaram'), 6, 9, 2700, 36);

-- ---------------------------------------------------------------- route 88 (suburban feeder)
INSERT INTO mtc_routes (route_number, direction, origin_stop_id, destination_stop_id, first_service, last_service, frequency_minutes, service_days, wheelchair_accessible, fare_min_minor, fare_max_minor)
VALUES ('88', 'UP', (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), (SELECT id FROM mtc_stops WHERE name = 'Vandalur'), '05:40', '21:40', 30, 'DAILY', 1, 500, 1000);
SET @r := LAST_INSERT_ID();
INSERT INTO mtc_route_stops (route_id, stop_id, stop_sequence, stage_number, fare_from_origin_minor, distance_km)
VALUES (@r, (SELECT id FROM mtc_stops WHERE name = 'Tambaram'), 1, 1, 0, 0),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Chromepet'), 2, 1, 500, 4),
       (@r, (SELECT id FROM mtc_stops WHERE name = 'Vandalur'), 3, 2, 1000, 9);
