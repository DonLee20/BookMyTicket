-- Demonstration reference data for the bus and railway modules.
-- Operators, trains and schedules are fictional samples (source = DEMO).
INSERT INTO bus_operators (name, code, premium, description, created_at, updated_at)
VALUES ('Bharat Yatra Coaches', 'BYC', 0, 'Value-focused interstate seater and semi-sleeper services.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('Southern Star Travels', 'SST', 0, 'South India sleeper and semi-sleeper network.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('Royal Coach Lines', 'RCL', 1, 'Premium Volvo-style multi-axle coaches with extra legroom.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('Deccan Cruiser', 'DCC', 0, 'Affordable AC and non-AC intercity services.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('Voltline Premium Coaches', 'VPC', 1, 'Luxury AC sleepers with attendant service.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('Coastal Connect', 'CCT', 0, 'Coastal and ghat-route specialist operator.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('Chettinad Travels', 'CHT', 1, 'Premium sleeper coaches on southern corridors.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('Capital Coachways', 'CCW', 1, 'North India premium and Volvo services.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('Western Trailways', 'WTL', 0, 'Mumbai–Pune–Goa western corridor services.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('Nilgiri Roadways', 'NGR', 0, 'Hill-route semi-sleeper services across the Nilgiris.', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO bus_corridors (city_a, city_b, distance_km, duration_minutes)
VALUES ('Chennai', 'Bengaluru', 350, 390),
       ('Chennai', 'Coimbatore', 510, 480),
       ('Chennai', 'Madurai', 460, 450),
       ('Chennai', 'Tirupati', 135, 180),
       ('Chennai', 'Puducherry', 160, 210),
       ('Chennai', 'Tiruchirappalli', 330, 360),
       ('Chennai', 'Hyderabad', 630, 660),
       ('Chennai', 'Kanyakumari', 700, 750),
       ('Bengaluru', 'Hyderabad', 575, 540),
       ('Bengaluru', 'Mysuru', 145, 180),
       ('Bengaluru', 'Goa', 560, 600),
       ('Bengaluru', 'Coimbatore', 360, 420),
       ('Bengaluru', 'Tirupati', 250, 300),
       ('Coimbatore', 'Ooty', 90, 180),
       ('Mumbai', 'Pune', 150, 210),
       ('Mumbai', 'Goa', 590, 660),
       ('Pune', 'Goa', 450, 600),
       ('Pune', 'Hyderabad', 560, 600),
       ('Delhi', 'Jaipur', 270, 330),
       ('Delhi', 'Manali', 540, 720),
       ('Jaipur', 'Udaipur', 400, 420),
       ('Hyderabad', 'Vijayawada', 275, 300),
       ('Kolkata', 'Digha', 185, 240),
       ('Kolkata', 'Darjeeling', 600, 720);

-- Schedules are direction-agnostic: the inventory seeder creates outbound and
-- return trips for every schedule, seven days a week.
SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Chennai' AND city_b = 'Bengaluru');
SET @op := (SELECT id FROM bus_operators WHERE code = 'RCL');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1320, 380, 129000, 'VOLVO_AC_SEMI_SLEEPER', 1, 'SEMI_SLEEPER_2X2',
        'AC,WIFI,CHARGING_POINT,WATER_BOTTLE,READING_LIGHT', 'Madiwala Check Post,Kempegowda Bus Station', 'Guindy,CMBT Koyambedu,Electronic City');
SET @op := (SELECT id FROM bus_operators WHERE code = 'SST');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1350, 420, 89000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'CMBT Koyambedu,Guindy,Perungalathur', 'Madiwala,Kempegowda Bus Station'),
       (@c, @op, 60, 400, 74000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'CMBT Koyambedu,Poonamallee', 'Electronic City,Madiwala'),
       (@c, @op, 1200, 390, 95000, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'CMBT Koyambedu,Guindy', 'Madiwala,Kempegowda Bus Station');
SET @op := (SELECT id FROM bus_operators WHERE code = 'BYC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1410, 420, 69900, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'CMBT Koyambedu', 'Madiwala');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Chennai' AND city_b = 'Coimbatore');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CHT');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1300, 480, 145000, 'AC_SLEEPER', 1, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET,READING_LIGHT,WATER_BOTTLE', 'CMBT Koyambedu,Guindy', 'Gandhipuram,Singanallur'),
       (@c, @op, 1260, 470, 139000, 'MULTI_AXLE_AC_SLEEPER', 1, 'SLEEPER_2X1', 'AC,WIFI,CHARGING_POINT,BLANKET,SNACKS', 'CMBT Koyambedu,Ashok Pillar', 'Gandhipuram');
SET @op := (SELECT id FROM bus_operators WHERE code = 'NGR');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 330, 450, 78000, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'CMBT Koyambedu', 'Gandhipuram,Singanallur'),
       (@c, @op, 1355, 500, 84000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'CMBT Koyambedu,Guindy', 'Gandhipuram');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Chennai' AND city_b = 'Madurai');
SET @op := (SELECT id FROM bus_operators WHERE code = 'SST');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1290, 450, 98000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'CMBT Koyambedu,Perungalathur', 'Mattuthavani,Arappalayam'),
       (@c, @op, 1380, 470, 89000, 'SLEEPER', 0, 'SLEEPER_2X1', 'CHARGING_POINT', 'CMBT Koyambedu', 'Mattuthavani');
SET @op := (SELECT id FROM bus_operators WHERE code = 'BYC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 300, 460, 62000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'CMBT Koyambedu,Tambaram', 'Mattuthavani,Arappalayam');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Chennai' AND city_b = 'Tirupati');
SET @op := (SELECT id FROM bus_operators WHERE code = 'DCC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 315, 190, 24000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'Chennai Central,Guindy,Poonamallee', 'Tirupati Bus Stand,Alipiri'),
       (@c, @op, 1140, 200, 22000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'Guindy,Poonamallee', 'Tirupati Bus Stand'),
       (@c, @op, 1320, 195, 26000, 'VOLVO_AC_SEMI_SLEEPER', 1, 'SEMI_SLEEPER_2X2', 'AC,WIFI,CHARGING_POINT,WATER_BOTTLE', 'Chennai Central,Guindy', 'Tirupati Bus Stand');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Chennai' AND city_b = 'Puducherry');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CCT');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 360, 210, 32000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'CMBT Koyambedu,Guindy', 'Puducherry Bus Stand'),
       (@c, @op, 1000, 225, 21000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'Guindy,Tambaram', 'Puducherry Bus Stand'),
       (@c, @op, 1110, 220, 29000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'CMBT Koyambedu', 'Puducherry Bus Stand');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Chennai' AND city_b = 'Tiruchirappalli');
SET @op := (SELECT id FROM bus_operators WHERE code = 'BYC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1370, 370, 71000, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'CMBT Koyambedu', 'Central Bus Stand Trichy,Srirangam'),
       (@c, @op, 400, 380, 52000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'CMBT Koyambedu,Tambaram', 'Central Bus Stand Trichy');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Chennai' AND city_b = 'Hyderabad');
SET @op := (SELECT id FROM bus_operators WHERE code = 'RCL');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1230, 660, 189000, 'MULTI_AXLE_AC_SLEEPER', 1, 'SLEEPER_2X1', 'AC,WIFI,CHARGING_POINT,BLANKET,READING_LIGHT,SNACKS', 'CMBT Koyambedu,Guindy', 'Miyapur,Kukatpally,Gachibowli');
SET @op := (SELECT id FROM bus_operators WHERE code = 'SST');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1300, 680, 139000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'CMBT Koyambedu,Perungalathur', 'Kukatpally,Miyapur');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Chennai' AND city_b = 'Kanyakumari');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CHT');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1080, 750, 159000, 'AC_SLEEPER', 1, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET,WATER_BOTTLE', 'CMBT Koyambedu,Tambaram', 'Nagercoil,Kanyakumari'),
       (@c, @op, 1200, 760, 118000, 'SLEEPER', 0, 'SLEEPER_2X1', 'CHARGING_POINT', 'CMBT Koyambedu', 'Nagercoil,Kanyakumari');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Bengaluru' AND city_b = 'Hyderabad');
SET @op := (SELECT id FROM bus_operators WHERE code = 'RCL');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1290, 540, 169000, 'VOLVO_AC_SEMI_SLEEPER', 1, 'SEMI_SLEEPER_2X2', 'AC,WIFI,CHARGING_POINT,READING_LIGHT,WATER_BOTTLE', 'Madiwala,Majestic', 'Kukatpally,Gachibowli');
SET @op := (SELECT id FROM bus_operators WHERE code = 'DCC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 300, 560, 95000, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'Majestic,Hebbal', 'Miyapur,Kukatpally'),
       (@c, @op, 1320, 550, 84900, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'Majestic', 'Miyapur');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Bengaluru' AND city_b = 'Mysuru');
SET @op := (SELECT id FROM bus_operators WHERE code = 'SST');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 360, 180, 32000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'Majestic,Satellite Bus Stand', 'Mysuru Bus Stand'),
       (@c, @op, 600, 185, 24000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'Majestic', 'Mysuru Bus Stand'),
       (@c, @op, 900, 175, 29900, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'Majestic,Satellite Bus Stand', 'Mysuru Bus Stand'),
       (@c, @op, 1080, 190, 27000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'Majestic', 'Mysuru Bus Stand');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Bengaluru' AND city_b = 'Goa');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CCT');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1230, 600, 149000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'Majestic,Yeshwanthpur', 'Panaji,Mapusa'),
       (@c, @op, 1300, 620, 129000, 'SLEEPER', 0, 'SLEEPER_2X1', 'CHARGING_POINT', 'Majestic', 'Panaji');
SET @op := (SELECT id FROM bus_operators WHERE code = 'VPC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1260, 590, 219000, 'MULTI_AXLE_AC_SLEEPER', 1, 'SLEEPER_2X1', 'AC,WIFI,CHARGING_POINT,BLANKET,READING_LIGHT,ENTERTAINMENT,SNACKS', 'Majestic,Yeshwanthpur', 'Panaji,Mapusa,Calangute');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Bengaluru' AND city_b = 'Coimbatore');
SET @op := (SELECT id FROM bus_operators WHERE code = 'NGR');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1310, 420, 99000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'Majestic,Electronic City', 'Gandhipuram,Singanallur'),
       (@c, @op, 330, 430, 71000, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'Majestic', 'Gandhipuram');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Bengaluru' AND city_b = 'Tirupati');
SET @op := (SELECT id FROM bus_operators WHERE code = 'DCC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 390, 300, 42000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'Majestic,HSR Layout', 'Tirupati Bus Stand'),
       (@c, @op, 1320, 310, 38000, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'Majestic', 'Tirupati Bus Stand');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Coimbatore' AND city_b = 'Ooty');
SET @op := (SELECT id FROM bus_operators WHERE code = 'NGR');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 375, 185, 18000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'Gandhipuram,Singanallur', 'Ooty Bus Stand'),
       (@c, @op, 780, 190, 16000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'Gandhipuram', 'Ooty Bus Stand'),
       (@c, @op, 1020, 180, 21000, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'Gandhipuram', 'Ooty Bus Stand');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Mumbai' AND city_b = 'Pune');
SET @op := (SELECT id FROM bus_operators WHERE code = 'WTL');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 400, 210, 34000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'Dadar,Borivali', 'Shivajinagar,Wakad'),
       (@c, @op, 630, 200, 26000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'Dadar', 'Shivajinagar'),
       (@c, @op, 1080, 215, 29900, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'Dadar,Borivali', 'Wakad,Shivajinagar'),
       (@c, @op, 1260, 220, 42000, 'VOLVO_AC_SEMI_SLEEPER', 1, 'SEMI_SLEEPER_2X2', 'AC,WIFI,CHARGING_POINT,READING_LIGHT', 'Dadar,Borivali', 'Shivajinagar,Hinjewadi');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Mumbai' AND city_b = 'Goa');
SET @op := (SELECT id FROM bus_operators WHERE code = 'VPC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1200, 660, 239000, 'MULTI_AXLE_AC_SLEEPER', 1, 'SLEEPER_2X1', 'AC,WIFI,CHARGING_POINT,BLANKET,READING_LIGHT,ENTERTAINMENT,SNACKS', 'Dadar,Borivali,Panvel', 'Panaji,Mapusa,Calangute');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CCT');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1140, 680, 149000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'Dadar,Panvel', 'Panaji,Mapusa');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Pune' AND city_b = 'Goa');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CCT');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1210, 600, 139000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'Shivajinagar,Wakad', 'Panaji,Mapusa'),
       (@c, @op, 1290, 620, 95000, 'SLEEPER', 0, 'SLEEPER_2X1', 'CHARGING_POINT', 'Shivajinagar', 'Panaji');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Pune' AND city_b = 'Hyderabad');
SET @op := (SELECT id FROM bus_operators WHERE code = 'DCC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1275, 610, 125000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'Shivajinagar,Wakad', 'Miyapur,Kukatpally'),
       (@c, @op, 1230, 630, 89000, 'SLEEPER', 0, 'SLEEPER_2X1', 'CHARGING_POINT', 'Shivajinagar', 'Miyapur');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Delhi' AND city_b = 'Jaipur');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CCW');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 360, 330, 54000, 'VOLVO_AC_SEMI_SLEEPER', 1, 'SEMI_SLEEPER_2X2', 'AC,WIFI,CHARGING_POINT,READING_LIGHT', 'Kashmere Gate,Dhaula Kuan,Gurugram', 'Sindhi Camp,Durgapura'),
       (@c, @op, 750, 320, 39000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'Kashmere Gate,Dhaula Kuan', 'Sindhi Camp'),
       (@c, @op, 1320, 340, 45900, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'Kashmere Gate,Gurugram', 'Sindhi Camp');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Delhi' AND city_b = 'Manali');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CCW');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1170, 720, 169000, 'VOLVO_AC_SEMI_SLEEPER', 1, 'SEMI_SLEEPER_2X2', 'AC,WIFI,CHARGING_POINT,BLANKET,READING_LIGHT', 'Kashmere Gate,Majnu ka Tila', 'Manali Volvo Stand,Mall Road'),
       (@c, @op, 1230, 740, 119000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'Kashmere Gate', 'Manali Volvo Stand');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Jaipur' AND city_b = 'Udaipur');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CCW');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 390, 420, 62000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'Sindhi Camp,Durgapura', 'Udaipur Bus Stand'),
       (@c, @op, 1350, 440, 82000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'Sindhi Camp', 'Udaipur Bus Stand');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Hyderabad' AND city_b = 'Vijayawada');
SET @op := (SELECT id FROM bus_operators WHERE code = 'DCC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 420, 300, 45000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'Miyapur,Kukatpally', 'Pandit Nehru Bus Station'),
       (@c, @op, 960, 310, 39000, 'SEMI_SLEEPER', 0, 'SEMI_SLEEPER_2X2', 'AC,CHARGING_POINT', 'Kukatpally', 'Pandit Nehru Bus Station'),
       (@c, @op, 1320, 320, 55000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'Miyapur', 'Pandit Nehru Bus Station');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Kolkata' AND city_b = 'Digha');
SET @op := (SELECT id FROM bus_operators WHERE code = 'BYC');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 360, 240, 30000, 'NON_AC_SEATER', 0, 'SEATER_2X2', 'CHARGING_POINT', 'Esplanade,Howrah', 'Digha Bus Stand'),
       (@c, @op, 690, 250, 36000, 'AC_SEATER', 0, 'SEATER_2X2', 'AC,CHARGING_POINT', 'Esplanade', 'Digha Bus Stand');

SET @c := (SELECT id FROM bus_corridors WHERE city_a = 'Kolkata' AND city_b = 'Darjeeling');
SET @op := (SELECT id FROM bus_operators WHERE code = 'CCW');
INSERT INTO bus_schedules (corridor_id, operator_id, depart_minutes, duration_minutes, fare_minor, bus_type, premium, layout, amenities, boarding_points, dropping_points)
VALUES (@c, @op, 1140, 720, 149000, 'AC_SLEEPER', 0, 'SLEEPER_2X1', 'AC,CHARGING_POINT,BLANKET', 'Esplanade,Howrah', 'Darjeeling Bus Stand'),
       (@c, @op, 1200, 740, 189000, 'VOLVO_AC_SEMI_SLEEPER', 1, 'SEMI_SLEEPER_2X2', 'AC,WIFI,CHARGING_POINT,BLANKET,READING_LIGHT', 'Esplanade', 'Darjeeling Bus Stand');

-- ---------------------------------------------------------------- railway stations
INSERT INTO railway_stations (code, name, city, state, zone)
VALUES ('MAS', 'MGR Chennai Central', 'Chennai', 'Tamil Nadu', 'SR'),
       ('MS', 'Chennai Egmore', 'Chennai', 'Tamil Nadu', 'SR'),
       ('TBM', 'Tambaram', 'Chennai', 'Tamil Nadu', 'SR'),
       ('CBE', 'Coimbatore Junction', 'Coimbatore', 'Tamil Nadu', 'SR'),
       ('SBC', 'KSR Bengaluru City', 'Bengaluru', 'Karnataka', 'SWR'),
       ('MYS', 'Mysuru Junction', 'Mysuru', 'Karnataka', 'SWR'),
       ('TPJ', 'Tiruchirappalli Junction', 'Tiruchirappalli', 'Tamil Nadu', 'SR'),
       ('MDU', 'Madurai Junction', 'Madurai', 'Tamil Nadu', 'SR'),
       ('SA', 'Salem Junction', 'Salem', 'Tamil Nadu', 'SR'),
       ('TVC', 'Thiruvananthapuram Central', 'Thiruvananthapuram', 'Kerala', 'SR'),
       ('ERS', 'Ernakulam Junction', 'Kochi', 'Kerala', 'SR'),
       ('SRR', 'Shoranur Junction', 'Shoranur', 'Kerala', 'SR'),
       ('CLT', 'Kozhikode', 'Kozhikode', 'Kerala', 'SR'),
       ('MAQ', 'Mangaluru Central', 'Mangaluru', 'Karnataka', 'SR'),
       ('BZA', 'Vijayawada Junction', 'Vijayawada', 'Andhra Pradesh', 'SCR'),
       ('SC', 'Secunderabad Junction', 'Hyderabad', 'Telangana', 'SCR'),
       ('HYB', 'Hyderabad Deccan', 'Hyderabad', 'Telangana', 'SCR'),
       ('KCG', 'Kacheguda', 'Hyderabad', 'Telangana', 'SCR'),
       ('PUNE', 'Pune Junction', 'Pune', 'Maharashtra', 'CR'),
       ('CSMT', 'Chhatrapati Shivaji Maharaj Terminus', 'Mumbai', 'Maharashtra', 'CR'),
       ('LTT', 'Lokmanya Tilak Terminus', 'Mumbai', 'Maharashtra', 'CR'),
       ('ADI', 'Ahmedabad Junction', 'Ahmedabad', 'Gujarat', 'WR'),
       ('JP', 'Jaipur Junction', 'Jaipur', 'Rajasthan', 'NWR'),
       ('NDLS', 'New Delhi', 'Delhi', 'Delhi', 'NR'),
       ('NZM', 'Hazrat Nizamuddin', 'Delhi', 'Delhi', 'NR'),
       ('BPL', 'Bhopal Junction', 'Bhopal', 'Madhya Pradesh', 'WCR'),
       ('NGP', 'Nagpur Junction', 'Nagpur', 'Maharashtra', 'CR'),
       ('BBS', 'Bhubaneswar', 'Bhubaneswar', 'Odisha', 'ECoR'),
       ('HWH', 'Howrah Junction', 'Kolkata', 'West Bengal', 'ER'),
       ('TPTY', 'Tirupati Main', 'Tirupati', 'Andhra Pradesh', 'SCR'),
       ('MAO', 'Madgaon Junction', 'Goa', 'Goa', 'KR'),
       ('QLN', 'Kollam Junction', 'Kollam', 'Kerala', 'SR');

-- ---------------------------------------------------------------- train services
SET @mas := (SELECT id FROM railway_stations WHERE code = 'MAS');
SET @cbe := (SELECT id FROM railway_stations WHERE code = 'CBE');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12675', 'Kovai Express', @mas, @cbe, 375, 830, 455, 'DAILY', 495),
       ('12676', 'Kovai Express', @cbe, (SELECT id FROM railway_stations WHERE code = 'MAS'), 915, 1365, 450, 'DAILY', 495);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12675');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_2_TIER', 121000, 'AVAILABLE', 18),
       (@svc, 'AC_3_TIER', 85000, 'AVAILABLE', 46),
       (@svc, 'AC_3_TIER_ECONOMY', 78000, 'AVAILABLE', 31),
       (@svc, 'SLEEPER', 32500, 'RAC', 0),
       (@svc, 'SECOND_SITTING', 16500, 'AVAILABLE', 88);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12676');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_2_TIER', 121000, 'AVAILABLE', 9),
       (@svc, 'AC_3_TIER', 85000, 'WAITLIST', 0),
       (@svc, 'AC_3_TIER_ECONOMY', 78000, 'AVAILABLE', 12),
       (@svc, 'SLEEPER', 32500, 'AVAILABLE', 54),
       (@svc, 'SECOND_SITTING', 16500, 'AVAILABLE', 120);
UPDATE train_service_classes SET wl_position = 21 WHERE service_id = @svc AND travel_class = 'AC_3_TIER';

SET @mys := (SELECT id FROM railway_stations WHERE code = 'MYS');
SET @mas := (SELECT id FROM railway_stations WHERE code = 'MAS');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12007', 'Mysuru Shatabdi Express', @mas, @mys, 360, 780, 420, 'MON,TUE,WED,THU,FRI,SAT', 500),
       ('12008', 'Mysuru Shatabdi Express', @mys, @mas, 855, 1305, 450, 'MON,TUE,WED,THU,FRI,SAT', 500);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12007');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'EXECUTIVE_CHAIR_CAR', 145000, 'AVAILABLE', 22),
       (@svc, 'CHAIR_CAR', 78500, 'AVAILABLE', 64),
       (@svc, 'AC_2_TIER', 132000, 'AVAILABLE', 14);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12008');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'EXECUTIVE_CHAIR_CAR', 145000, 'AVAILABLE', 16),
       (@svc, 'CHAIR_CAR', 78500, 'RAC', 0),
       (@svc, 'AC_2_TIER', 132000, 'AVAILABLE', 8);

SET @sbc := (SELECT id FROM railway_stations WHERE code = 'SBC');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12027', 'Bengaluru Shatabdi Express', @mas, @sbc, 1050, 1350, 300, 'MON,TUE,WED,THU,FRI,SAT', 362),
       ('12028', 'Bengaluru Shatabdi Express', @sbc, @mas, 360, 650, 290, 'MON,TUE,WED,THU,FRI,SAT', 362);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12027');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'EXECUTIVE_CHAIR_CAR', 129000, 'AVAILABLE', 28),
       (@svc, 'CHAIR_CAR', 70500, 'AVAILABLE', 72);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12028');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'EXECUTIVE_CHAIR_CAR', 129000, 'AVAILABLE', 11),
       (@svc, 'CHAIR_CAR', 70500, 'WAITLIST', 0);
UPDATE train_service_classes SET wl_position = 17 WHERE service_id = @svc AND travel_class = 'CHAIR_CAR';

INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12608', 'Lalbagh Express', @mas, @sbc, 395, 735, 340, 'DAILY', 362),
       ('12607', 'Lalbagh Express', @sbc, @mas, 930, 1270, 340, 'DAILY', 362);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12608');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'CHAIR_CAR', 48500, 'AVAILABLE', 96),
       (@svc, 'SECOND_SITTING', 22000, 'AVAILABLE', 180);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12607');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'CHAIR_CAR', 48500, 'AVAILABLE', 40),
       (@svc, 'SECOND_SITTING', 22000, 'WAITLIST', 0);
UPDATE train_service_classes SET wl_position = 32 WHERE service_id = @svc AND travel_class = 'SECOND_SITTING';

SET @ndls := (SELECT id FROM railway_stations WHERE code = 'NDLS');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12621', 'Tamil Nadu Express', @mas, @ndls, 1320, 1900, 1300, 'DAILY', 2175),
       ('12622', 'Tamil Nadu Express', @ndls, @mas, 1350, 2595, 1245, 'DAILY', 2175);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12621');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_FIRST', 465000, 'AVAILABLE', 4),
       (@svc, 'AC_2_TIER', 268000, 'AVAILABLE', 12),
       (@svc, 'AC_3_TIER', 187000, 'RAC', 0),
       (@svc, 'SLEEPER', 71000, 'AVAILABLE', 62);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12622');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_FIRST', 465000, 'WAITLIST', 0),
       (@svc, 'AC_2_TIER', 268000, 'AVAILABLE', 7),
       (@svc, 'AC_3_TIER', 187000, 'AVAILABLE', 24),
       (@svc, 'SLEEPER', 71000, 'AVAILABLE', 110);
UPDATE train_service_classes SET wl_position = 3 WHERE service_id = @svc AND travel_class = 'AC_FIRST';

SET @tvc := (SELECT id FROM railway_stations WHERE code = 'TVC');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12695', 'Chennai–Thiruvananthapuram Superfast Express', @mas, @tvc, 920, 1845, 925, 'DAILY', 925),
       ('12696', 'Thiruvananthapuram–Chennai Superfast Express', @tvc, @mas, 1035, 1960, 925, 'DAILY', 925);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12695');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_2_TIER', 196000, 'AVAILABLE', 21),
       (@svc, 'AC_3_TIER', 138000, 'AVAILABLE', 55),
       (@svc, 'SLEEPER', 52000, 'RAC', 0),
       (@svc, 'SECOND_SITTING', 29000, 'AVAILABLE', 140);

SET @tpty := (SELECT id FROM railway_stations WHERE code = 'TPTY');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('16053', 'Chennai–Tirupati Express', @mas, @tpty, 385, 580, 195, 'DAILY', 134),
       ('16054', 'Tirupati–Chennai Express', @tpty, @mas, 995, 1200, 205, 'DAILY', 134);
SET @svc := (SELECT id FROM train_services WHERE train_number = '16053');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'CHAIR_CAR', 22000, 'AVAILABLE', 120),
       (@svc, 'SECOND_SITTING', 9500, 'AVAILABLE', 210);
SET @svc := (SELECT id FROM train_services WHERE train_number = '16054');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'CHAIR_CAR', 22000, 'AVAILABLE', 98),
       (@svc, 'SECOND_SITTING', 9500, 'AVAILABLE', 175);

SET @mdu := (SELECT id FROM railway_stations WHERE code = 'MDU');
SET @ms := (SELECT id FROM railway_stations WHERE code = 'MS');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12635', 'Vaigai Express', @ms, @mdu, 800, 1265, 465, 'DAILY', 497),
       ('12636', 'Vaigai Express', @mdu, @ms, 425, 885, 460, 'DAILY', 497);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12635');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_3_TIER', 92000, 'AVAILABLE', 38),
       (@svc, 'CHAIR_CAR', 44500, 'AVAILABLE', 84),
       (@svc, 'SECOND_SITTING', 19500, 'AVAILABLE', 165);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12636');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_3_TIER', 92000, 'RAC', 0),
       (@svc, 'CHAIR_CAR', 44500, 'AVAILABLE', 51),
       (@svc, 'SECOND_SITTING', 19500, 'AVAILABLE', 190);

SET @ltt := (SELECT id FROM railway_stations WHERE code = 'LTT');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('11014', 'Coimbatore–Lokmanya Tilak Terminus Express', @cbe, @ltt, 495, 2105, 1610, 'DAILY', 1168);
SET @svc := (SELECT id FROM train_services WHERE train_number = '11014');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_2_TIER', 224000, 'AVAILABLE', 19),
       (@svc, 'AC_3_TIER', 158000, 'AVAILABLE', 60),
       (@svc, 'SLEEPER', 61000, 'AVAILABLE', 130);

SET @csmt := (SELECT id FROM railway_stations WHERE code = 'CSMT');
SET @pune := (SELECT id FROM railway_stations WHERE code = 'PUNE');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12123', 'Deccan Queen', @csmt, @pune, 1030, 1225, 195, 'DAILY', 192),
       ('12124', 'Deccan Queen', @pune, @csmt, 435, 625, 190, 'DAILY', 192);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12123');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'CHAIR_CAR', 34500, 'AVAILABLE', 110),
       (@svc, 'SECOND_SITTING', 17500, 'AVAILABLE', 190);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12124');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'CHAIR_CAR', 34500, 'AVAILABLE', 92),
       (@svc, 'SECOND_SITTING', 17500, 'AVAILABLE', 160);

INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12951', 'Mumbai Rajdhani', @csmt, @ndls, 1020, 1952, 932, 'DAILY', 1386),
       ('12952', 'Mumbai Rajdhani', @ndls, @csmt, 1015, 1935, 920, 'DAILY', 1386);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12951');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_FIRST', 488000, 'AVAILABLE', 6),
       (@svc, 'AC_2_TIER', 281000, 'AVAILABLE', 26),
       (@svc, 'AC_3_TIER', 196000, 'AVAILABLE', 74);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12952');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_FIRST', 488000, 'AVAILABLE', 2),
       (@svc, 'AC_2_TIER', 281000, 'RAC', 0),
       (@svc, 'AC_3_TIER', 196000, 'AVAILABLE', 40);

SET @sc := (SELECT id FROM railway_stations WHERE code = 'SC');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12723', 'Telangana Express', @sc, @ndls, 385, 1900, 1515, 'DAILY', 1660),
       ('12724', 'Telangana Express', @ndls, @sc, 960, 2500, 1540, 'DAILY', 1660);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12723');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_FIRST', 452000, 'AVAILABLE', 5),
       (@svc, 'AC_2_TIER', 261000, 'AVAILABLE', 14),
       (@svc, 'AC_3_TIER', 182000, 'WAITLIST', 0),
       (@svc, 'SLEEPER', 68000, 'AVAILABLE', 96);
UPDATE train_service_classes SET wl_position = 28 WHERE service_id = @svc AND travel_class = 'AC_3_TIER';
SET @svc := (SELECT id FROM train_services WHERE train_number = '12724');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_FIRST', 452000, 'AVAILABLE', 3),
       (@svc, 'AC_2_TIER', 261000, 'AVAILABLE', 20),
       (@svc, 'AC_3_TIER', 182000, 'AVAILABLE', 61),
       (@svc, 'SLEEPER', 68000, 'RAC', 0);

SET @kcg := (SELECT id FROM railway_stations WHERE code = 'KCG');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12785', 'Kacheguda–Bengaluru Express', @kcg, @sbc, 1145, 1830, 685, 'DAILY', 640),
       ('12786', 'Bengaluru–Kacheguda Express', @sbc, @kcg, 1100, 1780, 680, 'DAILY', 640);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12785');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_2_TIER', 152000, 'AVAILABLE', 23),
       (@svc, 'AC_3_TIER', 108000, 'AVAILABLE', 58),
       (@svc, 'SLEEPER', 42000, 'AVAILABLE', 122),
       (@svc, 'SECOND_SITTING', 21000, 'AVAILABLE', 160);

SET @adi := (SELECT id FROM railway_stations WHERE code = 'ADI');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('12009', 'Shatabdi Express (Mumbai–Ahmedabad)', @csmt, @adi, 385, 800, 415, 'MON,TUE,WED,THU,FRI,SAT', 493),
       ('12010', 'Shatabdi Express (Ahmedabad–Mumbai)', @adi, @csmt, 870, 1285, 415, 'MON,TUE,WED,THU,FRI,SAT', 493);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12009');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'EXECUTIVE_CHAIR_CAR', 142000, 'AVAILABLE', 30),
       (@svc, 'CHAIR_CAR', 76500, 'AVAILABLE', 88);

SET @mao := (SELECT id FROM railway_stations WHERE code = 'MAO');
INSERT INTO train_services (train_number, name, origin_station_id, destination_station_id, depart_minutes, arrive_minutes, duration_minutes, running_days, total_distance_km)
VALUES ('10103', 'Mandovi Express', @csmt, @mao, 430, 1235, 805, 'DAILY', 588),
       ('10104', 'Mandovi Express', @mao, @csmt, 555, 1365, 810, 'DAILY', 588);
SET @svc := (SELECT id FROM train_services WHERE train_number = '10103');
INSERT INTO train_service_classes (service_id, travel_class, fare_minor, availability_status, available_seats)
VALUES (@svc, 'AC_2_TIER', 168000, 'AVAILABLE', 17),
       (@svc, 'AC_3_TIER', 118000, 'AVAILABLE', 49),
       (@svc, 'SLEEPER', 45000, 'AVAILABLE', 101),
       (@svc, 'CHAIR_CAR', 36000, 'AVAILABLE', 90);

-- ---------------------------------------------------------------- intermediate stops (sample)
SET @svc := (SELECT id FROM train_services WHERE train_number = '12675');
INSERT INTO train_route_stops (service_id, station_id, stop_number, arrive_offset_minutes, depart_offset_minutes, distance_km, day_number)
VALUES (@svc, (SELECT id FROM railway_stations WHERE code = 'MAS'), 1, NULL, 0, 0, 1),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'SA'), 2, 240, 245, 335, 1),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'CBE'), 3, 455, NULL, 495, 1);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12007');
INSERT INTO train_route_stops (service_id, station_id, stop_number, arrive_offset_minutes, depart_offset_minutes, distance_km, day_number)
VALUES (@svc, (SELECT id FROM railway_stations WHERE code = 'MAS'), 1, NULL, 0, 0, 1),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'SBC'), 2, 290, 300, 362, 1),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'MYS'), 3, 420, NULL, 500, 1);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12621');
INSERT INTO train_route_stops (service_id, station_id, stop_number, arrive_offset_minutes, depart_offset_minutes, distance_km, day_number)
VALUES (@svc, (SELECT id FROM railway_stations WHERE code = 'MAS'), 1, NULL, 0, 0, 1),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'BZA'), 2, 400, 410, 431, 1),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'NGP'), 3, 900, 910, 1093, 2),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'BPL'), 4, 1150, 1155, 1497, 2),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'NDLS'), 5, 1300, NULL, 2175, 2);
SET @svc := (SELECT id FROM train_services WHERE train_number = '12695');
INSERT INTO train_route_stops (service_id, station_id, stop_number, arrive_offset_minutes, depart_offset_minutes, distance_km, day_number)
VALUES (@svc, (SELECT id FROM railway_stations WHERE code = 'MAS'), 1, NULL, 0, 0, 1),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'SA'), 2, 300, 305, 335, 1),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'ERS'), 3, 640, 650, 700, 1),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'QLN'), 4, 840, 845, 860, 2),
       (@svc, (SELECT id FROM railway_stations WHERE code = 'TVC'), 5, 925, NULL, 925, 2);
