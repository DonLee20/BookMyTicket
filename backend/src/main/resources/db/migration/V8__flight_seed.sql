-- Demonstration reference data for flights and Chennai MTC.
-- Airlines and flight numbers are fictional samples (source = DEMO).
INSERT INTO airports (iata, icao, name, city, country, country_code, region, timezone, latitude, longitude)
VALUES ('MAA', 'VOMM', 'Chennai International Airport', 'Chennai', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 12.994100, 80.170900),
       ('BLR', 'VOBL', 'Kempegowda International Airport', 'Bengaluru', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 13.198900, 77.706800),
       ('COK', 'VOCI', 'Cochin International Airport', 'Kochi', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 10.152000, 76.401900),
       ('TRV', 'VOTV', 'Thiruvananthapuram International Airport', 'Thiruvananthapuram', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 8.482100, 76.920100),
       ('HYD', 'VOHS', 'Rajiv Gandhi International Airport', 'Hyderabad', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 17.240300, 78.429400),
       ('BOM', 'VABB', 'Chhatrapati Shivaji Maharaj International Airport', 'Mumbai', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 19.088700, 72.867900),
       ('DEL', 'VIDP', 'Indira Gandhi International Airport', 'Delhi', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 28.556200, 77.100000),
       ('CCU', 'VECC', 'Netaji Subhas Chandra Bose International Airport', 'Kolkata', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 22.654700, 88.446700),
       ('GOI', 'VOGO', 'Goa Dabolim Airport', 'Goa', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 15.380800, 73.831400),
       ('AMD', 'VAAH', 'Sardar Vallabhbhai Patel International Airport', 'Ahmedabad', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 23.072200, 72.634700),
       ('PNQ', 'VAPO', 'Pune Airport', 'Pune', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 18.582100, 73.919700),
       ('JAI', 'VIJP', 'Jaipur International Airport', 'Jaipur', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 26.824200, 75.812200),
       ('LKO', 'VILK', 'Chaudhary Charan Singh International Airport', 'Lucknow', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 26.760600, 80.889300),
       ('IXC', 'VICG', 'Shaheed Bhagat Singh International Airport', 'Chandigarh', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 30.673500, 76.788500),
       ('TRZ', 'VOTR', 'Tiruchirappalli International Airport', 'Tiruchirappalli', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 10.765400, 78.709700),
       ('CJB', 'VOCB', 'Coimbatore International Airport', 'Coimbatore', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 11.030000, 77.043400),
       ('IXM', 'VOMD', 'Madurai International Airport', 'Madurai', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 9.834500, 78.093400),
       ('VTZ', 'VOVZ', 'Visakhapatnam Airport', 'Visakhapatnam', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 17.721200, 83.224500),
       ('BBI', 'VEBS', 'Biju Patnaik International Airport', 'Bhubaneswar', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 20.244400, 85.817800),
       ('PAT', 'VEPT', 'Jay Prakash Narayan International Airport', 'Patna', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 25.591300, 85.088000),
       ('GAU', 'VEGT', 'Lokpriya Gopinath Bordoloi International Airport', 'Guwahati', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 26.106100, 91.585900),
       ('SXR', 'VISR', 'Sheikh ul-Alam International Airport', 'Srinagar', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 33.987100, 74.774200),
       ('ATQ', 'VIAR', 'Sri Guru Ram Dass Jee International Airport', 'Amritsar', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 31.709600, 74.797300),
       ('NAG', 'VANP', 'Dr. Babasaheb Ambedkar International Airport', 'Nagpur', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 21.092200, 79.047200),
       ('IDR', 'VAID', 'Devi Ahilya Bai Holkar Airport', 'Indore', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 22.721800, 75.801100),
       ('RPR', 'VARP', 'Swami Vivekananda Airport', 'Raipur', 'India', 'IN', 'South Asia', 'Asia/Kolkata', 21.180400, 81.738800),
       ('DXB', 'OMDB', 'Dubai International Airport', 'Dubai', 'United Arab Emirates', 'AE', 'Middle East', 'Asia/Dubai', 25.253200, 55.365700),
       ('AUH', 'OMAA', 'Zayed International Airport', 'Abu Dhabi', 'United Arab Emirates', 'AE', 'Middle East', 'Asia/Dubai', 24.433000, 54.651100),
       ('DOH', 'OTHH', 'Hamad International Airport', 'Doha', 'Qatar', 'QA', 'Middle East', 'Asia/Qatar', 25.273100, 51.608100),
       ('MCT', 'OOMS', 'Muscat International Airport', 'Muscat', 'Oman', 'OM', 'Middle East', 'Asia/Muscat', 23.593300, 58.284400),
       ('RUH', 'OERK', 'King Khalid International Airport', 'Riyadh', 'Saudi Arabia', 'SA', 'Middle East', 'Asia/Riyadh', 24.957600, 46.698800),
       ('SIN', 'WSSS', 'Singapore Changi Airport', 'Singapore', 'Singapore', 'SG', 'South East Asia', 'Asia/Singapore', 1.364400, 103.991500),
       ('KUL', 'WMKK', 'Kuala Lumpur International Airport', 'Kuala Lumpur', 'Malaysia', 'MY', 'South East Asia', 'Asia/Kuala_Lumpur', 2.745600, 101.709900),
       ('BKK', 'VTBS', 'Suvarnabhumi Airport', 'Bangkok', 'Thailand', 'TH', 'South East Asia', 'Asia/Bangkok', 13.690000, 100.750100),
       ('CMB', 'VCBI', 'Bandaranaike International Airport', 'Colombo', 'Sri Lanka', 'LK', 'South Asia', 'Asia/Colombo', 7.180800, 79.884100),
       ('HKG', 'VHHH', 'Hong Kong International Airport', 'Hong Kong', 'Hong Kong', 'HK', 'East Asia', 'Asia/Hong_Kong', 22.308000, 113.918500),
       ('NRT', 'RJAA', 'Narita International Airport', 'Tokyo', 'Japan', 'JP', 'East Asia', 'Asia/Tokyo', 35.772000, 140.392900),
       ('LHR', 'EGLL', 'London Heathrow Airport', 'London', 'United Kingdom', 'GB', 'Europe', 'Europe/London', 51.470000, -0.454300),
       ('CDG', 'LFPG', 'Charles de Gaulle Airport', 'Paris', 'France', 'FR', 'Europe', 'Europe/Paris', 49.009700, 2.547900),
       ('FRA', 'EDDF', 'Frankfurt Airport', 'Frankfurt', 'Germany', 'DE', 'Europe', 'Europe/Berlin', 50.037900, 8.562200),
       ('AMS', 'EHAM', 'Amsterdam Airport Schiphol', 'Amsterdam', 'Netherlands', 'NL', 'Europe', 'Europe/Amsterdam', 52.310500, 4.768300),
       ('JFK', 'KJFK', 'John F. Kennedy International Airport', 'New York', 'United States', 'US', 'North America', 'America/New_York', 40.641300, -73.778100),
       ('LAX', 'KLAX', 'Los Angeles International Airport', 'Los Angeles', 'United States', 'US', 'North America', 'America/Los_Angeles', 33.941600, -118.408500),
       ('SYD', 'YSSY', 'Sydney Kingsford Smith Airport', 'Sydney', 'Australia', 'AU', 'Oceania', 'Australia/Sydney', -33.939900, 151.175300),
       ('MEL', 'YMML', 'Melbourne Airport', 'Melbourne', 'Australia', 'AU', 'Oceania', 'Australia/Melbourne', -37.669000, 144.841000);

-- Domestic schedules (fictional airlines)
SET @maa := (SELECT id FROM airports WHERE iata = 'MAA');
SET @blr := (SELECT id FROM airports WHERE iata = 'BLR');
SET @cok := (SELECT id FROM airports WHERE iata = 'COK');
SET @hyd := (SELECT id FROM airports WHERE iata = 'HYD');
SET @bom := (SELECT id FROM airports WHERE iata = 'BOM');
SET @del := (SELECT id FROM airports WHERE iata = 'DEL');
SET @goi := (SELECT id FROM airports WHERE iata = 'GOI');
SET @trz := (SELECT id FROM airports WHERE iata = 'TRZ');
SET @ixm := (SELECT id FROM airports WHERE iata = 'IXM');
SET @pnq := (SELECT id FROM airports WHERE iata = 'PNQ');

INSERT INTO flight_schedules (airline_code, airline_name, flight_number, origin_airport_id, destination_airport_id, depart_minutes, duration_minutes, aircraft, stops, base_fare_economy_minor, base_fare_premium_minor, base_fare_business_minor, base_fare_first_minor, baggage_note, refundable, changeable)
VALUES
    ('IW', 'IndiWings', 'IW-201', @maa, @blr, 390, 75, 'Airbus A320', 0, 459900, 689900, 1380000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-202', @blr, @maa, 510, 75, 'Airbus A320', 0, 459900, 689900, 1380000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-305', @maa, @cok, 555, 90, 'Airbus A320neo', 0, 529900, 789900, 1580000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-306', @cok, @maa, 795, 90, 'Airbus A320neo', 0, 529900, 789900, 1580000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-410', @blr, @del, 420, 165, 'Airbus A321', 0, 649900, 949900, 1890000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-411', @del, @blr, 720, 170, 'Airbus A321', 0, 649900, 949900, 1890000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-512', @maa, @del, 365, 165, 'Boeing 737-800', 0, 629900, 919900, 1840000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-513', @del, @maa, 675, 170, 'Boeing 737-800', 0, 629900, 919900, 1840000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-620', @maa, @hyd, 640, 90, 'Airbus A320', 0, 519900, 769900, 1520000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-621', @hyd, @maa, 840, 90, 'Airbus A320', 0, 519900, 769900, 1520000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-710', @blr, @bom, 680, 100, 'Airbus A320neo', 0, 549900, 819900, 1640000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-711', @bom, @blr, 880, 100, 'Airbus A320neo', 0, 549900, 819900, 1640000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-800', @bom, @del, 405, 125, 'Boeing 737 MAX 8', 0, 589900, 869900, 1740000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-801', @del, @bom, 650, 130, 'Boeing 737 MAX 8', 0, 589900, 869900, 1740000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-902', @hyd, @del, 570, 140, 'Airbus A321', 0, 609900, 889900, 1780000, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('IW', 'IndiWings', 'IW-903', @del, @hyd, 810, 145, 'Airbus A321', 0, 609900, 889900, 1780000, NULL, '15 kg check-in, 7 kg cabin', 0, 1);

SET @sbc2 := 0;

INSERT INTO flight_schedules (airline_code, airline_name, flight_number, origin_airport_id, destination_airport_id, depart_minutes, duration_minutes, aircraft, stops, base_fare_economy_minor, base_fare_premium_minor, base_fare_business_minor, base_fare_first_minor, baggage_note, refundable, changeable)
VALUES
    ('SD', 'SkyDeccan', 'SD-101', @blr, @hyd, 435, 80, 'ATR 72-600', 0, 389900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-102', @hyd, @blr, 660, 85, 'ATR 72-600', 0, 389900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-150', @maa, @trz, 790, 75, 'ATR 72-600', 0, 419900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-151', @trz, @maa, 940, 75, 'ATR 72-600', 0, 419900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-220', @bom, @pnq, 520, 55, 'ATR 72-600', 0, 319900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-221', @pnq, @bom, 700, 55, 'ATR 72-600', 0, 319900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-300', @maa, @ixm, 920, 95, 'ATR 72-600', 0, 469900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-301', @ixm, @maa, 1100, 95, 'ATR 72-600', 0, 469900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-410', @cok, @blr, 1085, 80, 'ATR 72-600', 0, 419900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-411', @blr, @cok, 900, 80, 'ATR 72-600', 0, 419900, NULL, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-500', @maa, @goi, 615, 105, 'Airbus A320', 0, 489900, 729900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-501', @goi, @maa, 800, 105, 'Airbus A320', 0, 489900, 729900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-520', @blr, @goi, 500, 70, 'Airbus A320', 0, 429900, 639900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('SD', 'SkyDeccan', 'SD-521', @goi, @blr, 640, 70, 'Airbus A320', 0, 429900, 639900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1);

INSERT INTO flight_schedules (airline_code, airline_name, flight_number, origin_airport_id, destination_airport_id, depart_minutes, duration_minutes, aircraft, stops, base_fare_economy_minor, base_fare_premium_minor, base_fare_business_minor, base_fare_first_minor, baggage_note, refundable, changeable)
VALUES
    ('CJ', 'Coastal Jet', 'CJ-120', @maa, (SELECT id FROM airports WHERE iata = 'VTZ'), 760, 85, 'Airbus A320', 0, 499900, 749900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('CJ', 'Coastal Jet', 'CJ-121', (SELECT id FROM airports WHERE iata = 'VTZ'), @maa, 930, 85, 'Airbus A320', 0, 499900, 749900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('CJ', 'Coastal Jet', 'CJ-130', (SELECT id FROM airports WHERE iata = 'CCU'), (SELECT id FROM airports WHERE iata = 'GAU'), 595, 85, 'Airbus A320', 0, 469900, 699900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('CJ', 'Coastal Jet', 'CJ-131', (SELECT id FROM airports WHERE iata = 'GAU'), (SELECT id FROM airports WHERE iata = 'CCU'), 760, 85, 'Airbus A320', 0, 469900, 699900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('CJ', 'Coastal Jet', 'CJ-140', @del, (SELECT id FROM airports WHERE iata = 'JAI'), 445, 65, 'Airbus A320', 0, 389900, 579900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('CJ', 'Coastal Jet', 'CJ-141', (SELECT id FROM airports WHERE iata = 'JAI'), @del, 590, 65, 'Airbus A320', 0, 389900, 579900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('CJ', 'Coastal Jet', 'CJ-150', @del, (SELECT id FROM airports WHERE iata = 'LKO'), 530, 80, 'Airbus A320', 0, 419900, 629900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1),
    ('CJ', 'Coastal Jet', 'CJ-151', (SELECT id FROM airports WHERE iata = 'LKO'), @del, 690, 80, 'Airbus A320', 0, 419900, 629900, NULL, NULL, '15 kg check-in, 7 kg cabin', 0, 1);

-- International schedules
INSERT INTO flight_schedules (airline_code, airline_name, flight_number, origin_airport_id, destination_airport_id, depart_minutes, duration_minutes, aircraft, stops, base_fare_economy_minor, base_fare_premium_minor, base_fare_business_minor, base_fare_first_minor, baggage_note, refundable, changeable)
VALUES
    ('MA', 'Meridian Airlines', 'MA-520', @maa, (SELECT id FROM airports WHERE iata = 'SIN'), 1370, 275, 'Airbus A350-900', 0, 1899900, 2699900, 5299900, NULL, '25 kg check-in, 7 kg cabin', 0, 1),
    ('MA', 'Meridian Airlines', 'MA-521', (SELECT id FROM airports WHERE iata = 'SIN'), @maa, 60, 285, 'Airbus A350-900', 0, 1899900, 2699900, 5299900, NULL, '25 kg check-in, 7 kg cabin', 0, 1),
    ('MA', 'Meridian Airlines', 'MA-540', @maa, (SELECT id FROM airports WHERE iata = 'CMB'), 740, 85, 'Airbus A320neo', 0, 799900, 1149900, 2199900, NULL, '20 kg check-in, 7 kg cabin', 0, 1),
    ('MA', 'Meridian Airlines', 'MA-541', (SELECT id FROM airports WHERE iata = 'CMB'), @maa, 900, 90, 'Airbus A320neo', 0, 799900, 1149900, 2199900, NULL, '20 kg check-in, 7 kg cabin', 0, 1),
    ('MA', 'Meridian Airlines', 'MA-560', @maa, (SELECT id FROM airports WHERE iata = 'KUL'), 1300, 240, 'Boeing 737 MAX 8', 0, 1599900, 2299900, 4499900, NULL, '20 kg check-in, 7 kg cabin', 0, 1),
    ('MA', 'Meridian Airlines', 'MA-561', (SELECT id FROM airports WHERE iata = 'KUL'), @maa, 200, 245, 'Boeing 737 MAX 8', 0, 1599900, 2299900, 4499900, NULL, '20 kg check-in, 7 kg cabin', 0, 1),
    ('MA', 'Meridian Airlines', 'MA-580', @maa, (SELECT id FROM airports WHERE iata = 'BKK'), 1410, 210, 'Airbus A320neo', 0, 1449900, 2099900, 3999900, NULL, '20 kg check-in, 7 kg cabin', 0, 1),
    ('MA', 'Meridian Airlines', 'MA-581', (SELECT id FROM airports WHERE iata = 'BKK'), @maa, 240, 215, 'Airbus A320neo', 0, 1449900, 2099900, 3999900, NULL, '20 kg check-in, 7 kg cabin', 0, 1),
    ('MA', 'Meridian Airlines', 'MA-501', @blr, (SELECT id FROM airports WHERE iata = 'SIN'), 1400, 265, 'Airbus A350-900', 0, 1949900, 2749900, 5399900, NULL, '25 kg check-in, 7 kg cabin', 0, 1),
    ('MA', 'Meridian Airlines', 'MA-502', (SELECT id FROM airports WHERE iata = 'SIN'), @blr, 90, 275, 'Airbus A350-900', 0, 1949900, 2749900, 5399900, NULL, '25 kg check-in, 7 kg cabin', 0, 1);

INSERT INTO flight_schedules (airline_code, airline_name, flight_number, origin_airport_id, destination_airport_id, depart_minutes, duration_minutes, aircraft, stops, base_fare_economy_minor, base_fare_premium_minor, base_fare_business_minor, base_fare_first_minor, baggage_note, refundable, changeable)
VALUES
    ('ZA', 'Zenith Airways', 'ZA-601', @maa, (SELECT id FROM airports WHERE iata = 'DXB'), 1210, 250, 'Boeing 787-9', 0, 2099900, 2999900, 6299900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('ZA', 'Zenith Airways', 'ZA-602', (SELECT id FROM airports WHERE iata = 'DXB'), @maa, 210, 255, 'Boeing 787-9', 0, 2099900, 2999900, 6299900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('ZA', 'Zenith Airways', 'ZA-620', @blr, (SELECT id FROM airports WHERE iata = 'DXB'), 1290, 250, 'Boeing 787-9', 0, 2049900, 2949900, 6199900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('ZA', 'Zenith Airways', 'ZA-621', (SELECT id FROM airports WHERE iata = 'DXB'), @blr, 230, 255, 'Boeing 787-9', 0, 2049900, 2949900, 6199900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('ZA', 'Zenith Airways', 'ZA-640', @del, (SELECT id FROM airports WHERE iata = 'DXB'), 1325, 215, 'Airbus A330-300', 0, 1899900, 2699900, 5699900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('ZA', 'Zenith Airways', 'ZA-641', (SELECT id FROM airports WHERE iata = 'DXB'), @del, 100, 220, 'Airbus A330-300', 0, 1899900, 2699900, 5699900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('ZA', 'Zenith Airways', 'ZA-660', @bom, (SELECT id FROM airports WHERE iata = 'DOH'), 1430, 200, 'Airbus A320neo', 0, 1699900, 2399900, 4999900, NULL, '25 kg check-in, 7 kg cabin', 0, 1),
    ('ZA', 'Zenith Airways', 'ZA-661', (SELECT id FROM airports WHERE iata = 'DOH'), @bom, 220, 205, 'Airbus A320neo', 0, 1699900, 2399900, 4999900, NULL, '25 kg check-in, 7 kg cabin', 0, 1),
    ('ZA', 'Zenith Airways', 'ZA-680', @hyd, (SELECT id FROM airports WHERE iata = 'AUH'), 1360, 240, 'Airbus A320neo', 0, 1799900, 2549900, 5299900, NULL, '25 kg check-in, 7 kg cabin', 0, 1),
    ('ZA', 'Zenith Airways', 'ZA-681', (SELECT id FROM airports WHERE iata = 'AUH'), @hyd, 180, 245, 'Airbus A320neo', 0, 1799900, 2549900, 5299900, NULL, '25 kg check-in, 7 kg cabin', 0, 1);

INSERT INTO flight_schedules (airline_code, airline_name, flight_number, origin_airport_id, destination_airport_id, depart_minutes, duration_minutes, aircraft, stops, base_fare_economy_minor, base_fare_premium_minor, base_fare_business_minor, base_fare_first_minor, baggage_note, refundable, changeable)
VALUES
    ('SA', 'Starlit Air', 'SA-801', @del, (SELECT id FROM airports WHERE iata = 'LHR'), 140, 555, 'Boeing 787-9', 0, 4699900, 6599900, 12999900, 17999900, '35 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-802', (SELECT id FROM airports WHERE iata = 'LHR'), @del, 650, 540, 'Boeing 787-9', 0, 4699900, 6599900, 12999900, 17999900, '35 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-820', @bom, (SELECT id FROM airports WHERE iata = 'LHR'), 170, 585, 'Airbus A350-1000', 0, 4799900, 6799900, 13499900, 18499900, '35 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-821', (SELECT id FROM airports WHERE iata = 'LHR'), @bom, 700, 570, 'Airbus A350-1000', 0, 4799900, 6799900, 13499900, 18499900, '35 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-840', @del, (SELECT id FROM airports WHERE iata = 'JFK'), 105, 900, 'Boeing 777-300ER', 0, 5999900, 8499900, 16999900, 23999900, '40 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-841', (SELECT id FROM airports WHERE iata = 'JFK'), @del, 120, 870, 'Boeing 777-300ER', 0, 5999900, 8499900, 16999900, 23999900, '40 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-860', @blr, (SELECT id FROM airports WHERE iata = 'FRA'), 80, 570, 'Airbus A350-900', 0, 4599900, 6499900, 12699900, NULL, '35 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-861', (SELECT id FROM airports WHERE iata = 'FRA'), @blr, 600, 555, 'Airbus A350-900', 0, 4599900, 6499900, 12699900, NULL, '35 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-880', @bom, (SELECT id FROM airports WHERE iata = 'SIN'), 70, 315, 'Airbus A330-300', 0, 2499900, 3499900, 6699900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-881', (SELECT id FROM airports WHERE iata = 'SIN'), @bom, 420, 320, 'Airbus A330-300', 0, 2499900, 3499900, 6699900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-900', @del, (SELECT id FROM airports WHERE iata = 'CDG'), 150, 570, 'Boeing 787-9', 0, 4499900, 6399900, 12499900, NULL, '35 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-901', (SELECT id FROM airports WHERE iata = 'CDG'), @del, 660, 555, 'Boeing 787-9', 0, 4499900, 6399900, 12499900, NULL, '35 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-930', @del, (SELECT id FROM airports WHERE iata = 'NRT'), 50, 450, 'Boeing 787-8', 0, 3999900, 5699900, 10999900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-931', (SELECT id FROM airports WHERE iata = 'NRT'), @del, 540, 480, 'Boeing 787-8', 0, 3999900, 5699900, 10999900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-940', @blr, (SELECT id FROM airports WHERE iata = 'HKG'), 40, 330, 'Airbus A330-300', 0, 2599900, 3649900, 6999900, NULL, '30 kg check-in, 7 kg cabin', 0, 1),
    ('SA', 'Starlit Air', 'SA-941', (SELECT id FROM airports WHERE iata = 'HKG'), @blr, 400, 340, 'Airbus A330-300', 0, 2599900, 3649900, 6999900, NULL, '30 kg check-in, 7 kg cabin', 0, 1);
