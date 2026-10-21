-- Demonstration tourism catalog. All packages below are clearly labelled sample
-- inventory (source = DEMO). Prices are per person in INR unless stated.
INSERT INTO destinations (slug, name, region, country, kind, hero_image_url, summary, description, best_season, featured, created_at, updated_at)
VALUES ('munnar', 'Munnar', 'Kerala', 'India', 'DOMESTIC', '/images/destinations/munnar.jpg',
        'Rolling tea estates, misty hill roads and quiet colonial-era bungalows in the Western Ghats.',
        'Munnar sits at 1,600 m in the Western Ghats and is best explored slowly: tea museum, Eravikulam National Park, Mattupetty dam and long walks through tea country.',
        'September to May', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('ooty', 'Ooty', 'Tamil Nadu', 'India', 'DOMESTIC', '/images/destinations/ooty.jpg',
        'The Nilgiri hill station of botanical gardens, toy train rides and eucalyptus air.',
        'Ooty blends colonial architecture with lake views, the UNESCO Nilgiri Mountain Railway and easy access to Coonoor and Doddabetta peak.',
        'October to June', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('goa', 'Goa', 'Goa', 'India', 'DOMESTIC', '/images/destinations/goa.jpg',
        'Palm-fringed beaches in the north, Latin quarter heritage and river cruises in the south.',
        'From the forts and shacks of the north to the whitewashed churches of Old Goa and the spice farms of Ponda.',
        'November to February', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('kerala-backwaters', 'Alleppey & Kumarakom', 'Kerala', 'India', 'DOMESTIC', '/images/destinations/kerala-backwaters.jpg',
        'Houseboat nights on the Vembanad lake network and village life along the canals.',
        'The backwaters of Alappuzha and Kumarakom are a maze of canals, paddy fields and coir villages best seen from a traditional kettuvallam.',
        'September to March', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('jaipur', 'Jaipur', 'Rajasthan', 'India', 'DOMESTIC', '/images/destinations/jaipur.jpg',
        'Amber Fort, Hawa Mahal and the bazaars of the Pink City.',
        'Rajasthan''s capital pairs Rajput architecture with a serious craft and food scene, and makes an easy long weekend from Delhi.',
        'October to March', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('manali', 'Manali & Solang', 'Himachal Pradesh', 'India', 'DOMESTIC', '/images/destinations/manali.jpg',
        'Deodar forests, river rafting and high passes of the Kullu valley.',
        'Manali is the gateway to Solang valley, Rohtang and the Spiti road, with river rafting on the Beas and quiet villages like Naggar.',
        'March to June, September to November', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('coorg', 'Coorg', 'Karnataka', 'India', 'DOMESTIC', '/images/destinations/coorg.jpg',
        'Coffee plantations, homestays and waterfalls in the Western Ghats.',
        'Madikeri, Abbey Falls, Dubare elephant camp and endless coffee estate walks make Coorg a favourite weekend break from Bengaluru.',
        'October to March', 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('andaman', 'Andaman Islands', 'Andaman & Nicobar', 'India', 'DOMESTIC', '/images/destinations/andaman.jpg',
        'Reef snorkelling, Radhanagar sunsets and rainforest trails.',
        'Havelock and Neil islands offer some of India''s clearest water, with scuba diving, sea walking and quiet mangrove creeks.',
        'October to May', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('ranthambore', 'Ranthambore', 'Rajasthan', 'India', 'DOMESTIC', '/images/destinations/ranthambore.jpg',
        'Tiger safaris around a 10th-century fort in dry deciduous forest.',
        'One of India''s best-known tiger reserves, with morning and afternoon gypsy safaris across zones around the fort and lakes.',
        'October to April', 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('bali', 'Bali', 'Bali', 'Indonesia', 'INTERNATIONAL', '/images/destinations/bali.jpg',
        'Rice terraces, temple ceremonies and surf beaches.',
        'Ubud''s terraces and temples, the volcanic sunrise at Mount Batur and the beach towns of the south.',
        'April to October', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('kyoto', 'Kyoto', 'Kansai', 'Japan', 'INTERNATIONAL', '/images/destinations/kyoto.jpg',
        'Shrines, bamboo groves, machiya lanes and kaiseki.',
        'Kyoto rewards slow travel: Fushimi Inari at dawn, Arashiyama, Nishiki market and the philosopher''s path.',
        'March to May, October to November', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('singapore', 'Singapore', 'Singapore', 'Singapore', 'INTERNATIONAL', '/images/destinations/singapore.jpg',
        'Garden city with hawker food, Sentosa and a compact metro.',
        'Gardens by the Bay, the Botanic Gardens, Little India and Sentosa make Singapore an easy first family trip abroad.',
        'All year', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('dubai', 'Dubai', 'Dubai', 'United Arab Emirates', 'INTERNATIONAL', '/images/destinations/dubai.jpg',
        'Desert dunes, old creek markets and record-setting skylines.',
        'Dubai mixes the gold and spice souks of Deira with dune safaris, museums and beach resorts.',
        'November to March', 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('darjeeling', 'Darjeeling', 'West Bengal', 'India', 'DOMESTIC', '/images/destinations/darjeeling.jpg',
        'Tea gardens, Kanchenjunga views and the toy train.',
        'Darjeeling''s tea estates, Observatory Hill and the UNESCO Darjeeling Himalayan Railway make a classic eastern Himalayan break.',
        'March to May, October to December', 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

-- ---------------------------------------------------------------- Munnar
SET @munnar := (SELECT id FROM destinations WHERE slug = 'munnar');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('munnar-tea-trails-4d', 'Munnar Tea Trails & Eravikulam', @munnar, 'FAMILY',
        'Four relaxed days across tea estates, Eravikulam National Park and the Mattupetty hills with a private car.',
        'A slow-paced family itinerary with a tea-estate walk, a visit to Eravikulam for the Nilgiri tahr, and time in Munnar town. Accommodation in a 3-star estate-view hotel with breakfast.',
        4, 3, 1890000, 'INR', '/images/destinations/munnar.jpg', 1, 0, 1, 1, 1, 1, 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Kochi, drive to Munnar', 'Pickup from Kochi airport or railway station and a scenic 4-hour drive with a stop at Cheeyappara falls. Evening free in Munnar town.', 'Dinner'),
       (@p, 2, 'Tea estate walk and tea museum', 'Guided walk through a working tea estate, followed by the KDHP tea museum and a tasting session.', 'Breakfast'),
       (@p, 3, 'Eravikulam National Park and Mattupetty', 'Morning permit-based visit to Eravikulam National Park, afternoon at Mattupetty dam and Echo Point.', 'Breakfast'),
       (@p, 4, 'Top Station viewpoint, return to Kochi', 'Sunrise drive to Top Station when weather permits, then transfer back to Kochi.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '3 nights in a 3-star hotel with breakfast'),
       (@p, 'Private air-conditioned car with driver for all transfers and sightseeing'),
       (@p, 'Eravikulam National Park entry permits and tea museum tickets'),
       (@p, 'All applicable tolls, parking and driver allowance');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Airfare or train fare to Kochi'),
       (@p, 'Lunch, dinner and personal expenses'),
       (@p, 'Anything not listed under inclusions');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Guided working tea estate walk'),
       (@p, 'Nilgiri tahr at Eravikulam'),
       (@p, 'Top Station valley views');

-- ---------------------------------------------------------------- Kerala backwaters
SET @kb := (SELECT id FROM destinations WHERE slug = 'kerala-backwaters');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('kerala-backwaters-houseboat-5d', 'Kerala Backwaters & Kochi Honeymoon', @kb, 'HONEYMOON',
        'Fort Kochi heritage, a private houseboat night on the Vembanad backwaters and an Ayurvedic couple''s massage.',
        'A five-day honeymoon route: two nights in Fort Kochi, one night on a private air-conditioned houseboat and a final night in Kumarakom. Candlelight dinner and couple''s massage included.',
        5, 4, 3250000, 'INR', '/images/destinations/kerala-backwaters.jpg', 1, 0, 1, 1, 1, 1, 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Kochi, Fort Kochi walk', 'Check in at a boutique heritage property and walk the Chinese fishing nets, St Francis Church and Jew Town.', 'Dinner'),
       (@p, 2, 'Kochi museums and Kathakali', 'Mattancherry Palace, the Dutch cemetery and an evening Kathakali performance.', 'Breakfast, dinner'),
       (@p, 3, 'Board private houseboat at Alleppey', 'Drive to Alleppey and board an air-conditioned private houseboat with a chef for an overnight cruise.', 'Breakfast, lunch, dinner'),
       (@p, 4, 'Kumarakom village and bird sanctuary', 'Disembark after breakfast, drive to Kumarakom for a village canoe ride and the bird sanctuary.', 'Breakfast, dinner'),
       (@p, 5, 'Ayurvedic massage, departure', 'Couple''s Ayurvedic massage before the onward transfer to Kochi airport.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '4 nights in heritage hotels and a private houseboat'),
       (@p, 'Daily breakfast plus selected meals as listed'),
       (@p, 'Private houseboat cruise with crew and chef'),
       (@p, 'Airport transfers and all sightseeing by private car');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Airfare or train fare to Kochi'),
       (@p, 'Drinks, tips and personal expenses'),
       (@p, 'Travel insurance');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Private overnight houseboat'),
       (@p, 'Fort Kochi heritage walk'),
       (@p, 'Couple''s Ayurvedic massage');

-- ---------------------------------------------------------------- Ooty
SET @ooty := (SELECT id FROM destinations WHERE slug = 'ooty');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('ooty-nilgiri-weekend-3d', 'Ooty & Coonoor Weekend', @ooty, 'WEEKEND',
        'A three-day Nilgiri break with the heritage toy train, botanical gardens and tea factory visit.',
        'Depart Friday night or Saturday morning from Chennai or Bengaluru; return Sunday. Includes two nights'' stay, toy train joyride subject to availability and a Coonoor tea factory tour.',
        3, 2, 1190000, 'INR', '/images/destinations/ooty.jpg', 1, 0, 1, 0, 1, 1, 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Ooty, lake and gardens', 'Check in, then Ooty lake boating and the Government Botanical Gardens.', 'Dinner'),
       (@p, 2, 'Toy train and Coonoor', 'Morning Nilgiri Mountain Railway joyride to Coonoor, visit Sim''s Park and a tea factory.', 'Breakfast'),
       (@p, 3, 'Doddabetta peak, departure', 'Sunrise drive to Doddabetta, then checkout and departure.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '2 nights in a 3-star hotel with breakfast'),
       (@p, 'Local sightseeing by private car'),
       (@p, 'Nilgiri Mountain Railway joyride tickets (subject to availability)');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Transport to and from Ooty'),
       (@p, 'Entry tickets not listed'),
       (@p, 'Lunch and dinner');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'UNESCO heritage toy train'),
       (@p, 'Doddabetta sunrise'),
       (@p, 'Coonoor tea factory');

-- ---------------------------------------------------------------- Goa
SET @goa := (SELECT id FROM destinations WHERE slug = 'goa');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('goa-beach-heritage-5d', 'Goa Beaches & Latin Quarter', @goa, 'FAMILY',
        'Five days split between North Goa beaches and the heritage lanes of Panaji and Old Goa.',
        'Stay two nights in Candolim and two in Panaji. Includes a Mandovi river cruise, spice plantation lunch and a guided Old Goa walk.',
        5, 4, 2490000, 'INR', '/images/destinations/goa.jpg', 1, 0, 1, 1, 1, 1, 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Goa, Candolim', 'Airport pickup and check in near Candolim beach. Evening at leisure.', 'Dinner'),
       (@p, 2, 'North Goa forts and beaches', 'Aguada and Chapora forts, Anjuna and Vagator beaches.', 'Breakfast'),
       (@p, 3, 'Spice plantation and river cruise', 'Visit a working spice plantation with lunch, then an evening Mandovi cruise.', 'Breakfast, lunch'),
       (@p, 4, 'Old Goa and Panaji walk', 'Basilica of Bom Jesus, Se Cathedral and a guided Fontainhas Latin quarter walk.', 'Breakfast'),
       (@p, 5, 'Departure', 'Checkout and airport transfer.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '4 nights in 3-star hotels with breakfast'),
       (@p, 'Airport transfers and sightseeing by private car'),
       (@p, 'Spice plantation entry with lunch'),
       (@p, 'Mandovi river cruise tickets');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Flights or trains to Goa'),
       (@p, 'Watersports and adventure activities'),
       (@p, 'Personal expenses and tips');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Fontainhas heritage walk'),
       (@p, 'Mandovi sunset cruise'),
       (@p, 'Spice plantation lunch');

-- ---------------------------------------------------------------- Jaipur
SET @jaipur := (SELECT id FROM destinations WHERE slug = 'jaipur');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('jaipur-heritage-4d', 'Jaipur Pink City Heritage', @jaipur, 'CULTURAL',
        'Amber Fort, Hawa Mahal, block printing and a Rajasthani cooking session in four days.',
        'A compact heritage break with a licensed guide, a block-printing workshop in Sanganer and dinner at a heritage haveli.',
        4, 3, 1950000, 'INR', '/images/destinations/jaipur.jpg', 1, 0, 1, 1, 0, 1, 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Jaipur, old city', 'Check in and an evening walk through Johari and Bapu bazaars ending at Hawa Mahal.', ''),
       (@p, 2, 'Amber Fort and Jal Mahal', 'Morning at Amber Fort with a guide, afternoon at Jal Mahal and the Panna Meena stepwell.', 'Breakfast'),
       (@p, 3, 'City Palace and block printing', 'City Palace and Jantar Mantar, then a block-printing workshop in Sanganer.', 'Breakfast'),
       (@p, 4, 'Cooking session, departure', 'Rajasthani cooking session followed by departure transfer.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '3 nights in a heritage 3-star hotel with breakfast'),
       (@p, 'Licensed local guide for monument visits'),
       (@p, 'Private car with driver for all days'),
       (@p, 'Block-printing workshop and cooking session');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Monument entry tickets'),
       (@p, 'Travel to and from Jaipur'),
       (@p, 'Meals not mentioned');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Amber Fort with guide'),
       (@p, 'Sanganer block printing'),
       (@p, 'Rajasthani cooking session');

-- ---------------------------------------------------------------- Manali
SET @manali := (SELECT id FROM destinations WHERE slug = 'manali');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('manali-solang-adventure-6d', 'Manali, Solang & River Rafting', @manali, 'ADVENTURE',
        'Six active days with rafting on the Beas, Solang valley activities and a Naggar castle walk.',
        'Built for travellers who want movement: rafting, paragliding at Solang (weather dependent), a Jogini falls hike and Old Manali cafés.',
        6, 5, 2790000, 'INR', '/images/destinations/manali.jpg', 1, 0, 1, 1, 1, 1, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Manali', 'Check in at Old Manali and acclimatise with a short village walk.', 'Dinner'),
       (@p, 2, 'Solang valley', 'Full day at Solang with optional paragliding and zorbing, subject to weather.', 'Breakfast'),
       (@p, 3, 'Beas river rafting', 'Grade II-III rafting stretch followed by an evening at the Mall Road.', 'Breakfast'),
       (@p, 4, 'Jogini falls hike', 'Guided hike to Jogini falls through deodar forest.', 'Breakfast'),
       (@p, 5, 'Naggar castle and art gallery', 'Visit Naggar castle, the Roerich art gallery and a local orchard.', 'Breakfast'),
       (@p, 6, 'Departure', 'Checkout and transfer to the bus stand or airport.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '5 nights in a 3-star hotel with breakfast'),
       (@p, 'Rafting session with certified guides and equipment'),
       (@p, 'Guided Jogini falls hike'),
       (@p, 'Local transfers by private car');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Paragliding and other optional adventure activities'),
       (@p, 'Travel to and from Manali'),
       (@p, 'Lunch and dinner unless listed');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Beas river rafting'),
       (@p, 'Solang valley day'),
       (@p, 'Old Manali cafés');

-- ---------------------------------------------------------------- Coorg
SET @coorg := (SELECT id FROM destinations WHERE slug = 'coorg');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('coorg-coffee-country-3d', 'Coorg Coffee Country Break', @coorg, 'WEEKEND',
        'Two nights on a working coffee estate with plantation walks and Abbey Falls.',
        'A weekend homestay break from Bengaluru or Mangaluru: estate walk, coffee cupping session, Abbey Falls and Madikeri fort.',
        3, 2, 1390000, 'INR', '/images/destinations/coorg.jpg', 1, 0, 1, 1, 1, 1, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Madikeri', 'Check in to the estate homestay and take a guided plantation walk followed by a coffee cupping session.', 'Dinner'),
       (@p, 2, 'Abbey Falls and Raja''s Seat', 'Abbey Falls, Madikeri fort and sunset at Raja''s Seat.', 'Breakfast, dinner'),
       (@p, 3, 'Dubare and departure', 'Optional Dubare elephant camp visit before departure.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '2 nights on a working coffee estate with breakfast and dinner'),
       (@p, 'Guided plantation walk with coffee tasting'),
       (@p, 'Local transfers by private car');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Travel to and from Coorg'),
       (@p, 'Dubare camp activities'),
       (@p, 'Lunch and personal expenses');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Working coffee estate stay'),
       (@p, 'Abbey Falls'),
       (@p, 'Raja''s Seat sunset');

-- ---------------------------------------------------------------- Andaman
SET @andaman := (SELECT id FROM destinations WHERE slug = 'andaman');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('andaman-island-escape-6d', 'Andaman Island Escape', @andaman, 'HONEYMOON',
        'Radhanagar sunsets, reef snorkelling at Elephant Beach and a private candlelight dinner on Havelock.',
        'Six days across Port Blair, Havelock and Neil islands with a cellular jail visit, ferry transfers and snorkelling included.',
        6, 5, 4690000, 'INR', '/images/destinations/andaman.jpg', 1, 0, 1, 1, 1, 1, 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Port Blair', 'Cellular jail visit and the light-and-sound show in the evening.', 'Dinner'),
       (@p, 2, 'Ferry to Havelock, Radhanagar', 'Morning ferry, check in and sunset at Radhanagar beach.', 'Breakfast, dinner'),
       (@p, 3, 'Elephant Beach snorkelling', 'Boat ride to Elephant Beach with snorkelling and optional sea walking.', 'Breakfast, dinner'),
       (@p, 4, 'Neil Island', 'Ferry to Neil, visit Bharatpur and Laxmanpur beaches.', 'Breakfast, dinner'),
       (@p, 5, 'Return to Port Blair, candlelight dinner', 'Ferry back, free afternoon and a private candlelight dinner.', 'Breakfast, dinner'),
       (@p, 6, 'Departure', 'Transfer to the airport.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '5 nights in sea-view hotels with breakfast'),
       (@p, 'All inter-island ferry tickets (private or government ferry)'),
       (@p, 'Snorkelling at Elephant Beach with equipment'),
       (@p, 'Airport transfers and sightseeing by private car');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Flights to and from Port Blair'),
       (@p, 'Scuba diving and sea walking'),
       (@p, 'Lunch and personal expenses');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Radhanagar beach sunset'),
       (@p, 'Elephant Beach snorkelling'),
       (@p, 'Private candlelight dinner');

-- ---------------------------------------------------------------- Ranthambore
SET @ranthambore := (SELECT id FROM destinations WHERE slug = 'ranthambore');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('ranthambore-tiger-trail-3d', 'Ranthambore Tiger Trail', @ranthambore, 'WILDLIFE',
        'Three days with two shared jeep safaris in Ranthambore National Park and a fort visit.',
        'Two safari drives across different zones (subject to forest department permits) with naturalists, plus the Ranthambore fort and Trinetra Ganesh temple.',
        3, 2, 2190000, 'INR', '/images/destinations/ranthambore.jpg', 1, 0, 1, 1, 1, 1, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Sawai Madhopur, afternoon safari', 'Check in to a wildlife lodge and take an afternoon shared jeep safari.', 'Dinner'),
       (@p, 2, 'Morning safari and Ranthambore fort', 'Morning safari, afternoon visit to the fort and Trinetra Ganesh temple.', 'Breakfast, dinner'),
       (@p, 3, 'Departure', 'Breakfast and transfer to Sawai Madhopur station.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '2 nights in a wildlife lodge with breakfast and dinner'),
       (@p, 'Two shared jeep safaris with naturalist (zone subject to permits)'),
       (@p, 'Ranthambore fort and temple visit'),
       (@p, 'Station transfers');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Travel to and from Sawai Madhopur'),
       (@p, 'Camera fees and tips'),
       (@p, 'Meals not mentioned');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Two jeep safaris'),
       (@p, 'Ranthambore fort'),
       (@p, 'Naturalist-led drives');

-- ---------------------------------------------------------------- Bali
SET @bali := (SELECT id FROM destinations WHERE slug = 'bali');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('bali-island-romance-7d', 'Bali Island Romance (Land Only)', @bali, 'HONEYMOON',
        'Seven land-only days across Ubud and Seminyak with a Mount Batur sunrise and a private villa night.',
        'A land-only honeymoon package: stays, private car with driver, Batur sunrise trek and a floating breakfast. International airfare is NOT included.',
        7, 6, 8990000, 'INR', '/images/destinations/bali.jpg', 1, 0, 1, 1, 1, 1, 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Denpasar, transfer to Ubud', 'Airport meet and transfer to an Ubud valley-view resort.', 'Dinner'),
       (@p, 2, 'Ubud rice terraces and temples', 'Tegalalang terraces, Tirta Empul and the Ubud monkey forest.', 'Breakfast'),
       (@p, 3, 'Mount Batur sunrise trek', 'Early sunrise trek with a guide, afternoon at leisure or spa.', 'Breakfast'),
       (@p, 4, 'Ubud to Seminyak', 'Transfer to Seminyak with a stop at Tanah Lot temple.', 'Breakfast'),
       (@p, 5, 'South Bali beaches', 'Uluwatu temple, Padang Padang beach and a Kecak dance at sunset.', 'Breakfast'),
       (@p, 6, 'Free day and floating breakfast', 'Private villa day with a floating breakfast and couple''s spa.', 'Breakfast'),
       (@p, 7, 'Departure', 'Transfer to Denpasar airport.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '6 nights in 4-star resorts with breakfast'),
       (@p, 'Private car with English-speaking driver for transfers and sightseeing'),
       (@p, 'Mount Batur sunrise trek with guide and breakfast'),
       (@p, 'Couple''s spa session and floating breakfast');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'International airfare to and from Bali (land-only package)'),
       (@p, 'Visa on arrival, if applicable'),
       (@p, 'Lunch, dinner and personal expenses'),
       (@p, 'Travel insurance');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Mount Batur sunrise'),
       (@p, 'Private villa night'),
       (@p, 'Uluwatu Kecak sunset');

-- ---------------------------------------------------------------- Kyoto
SET @kyoto := (SELECT id FROM destinations WHERE slug = 'kyoto');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('kyoto-culture-7d', 'Kyoto Culture & Craft (Land Only)', @kyoto, 'CULTURAL',
        'Seven land-only days of temples, craft workshops and kaiseki in Kyoto with a Nara day trip.',
        'Small-group cultural journey: Fushimi Inari before the crowds, Arashiyama, a tea ceremony, a knife-making visit in Sakai and a Nara day trip. International airfare is NOT included.',
        7, 6, 15900000, 'INR', '/images/destinations/kyoto.jpg', 1, 0, 1, 1, 1, 1, 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Kyoto', 'Airport express transfer and an orientation walk near Gion.', ''),
       (@p, 2, 'Fushimi Inari and southern temples', 'Early Fushimi Inari hike, then Tofuku-ji and Sanjusangen-do.', 'Breakfast'),
       (@p, 3, 'Arashiyama and tea ceremony', 'Bamboo grove, Tenryu-ji and a private tea ceremony.', 'Breakfast'),
       (@p, 4, 'Nara day trip', 'Todai-ji, Kasuga shrine and Nara park deer.', 'Breakfast'),
       (@p, 5, 'Craft day', 'Nishijin weaving workshop and a knife-sharpening demonstration.', 'Breakfast'),
       (@p, 6, 'Northern Kyoto', 'Kinkaku-ji, Ryoan-ji and the philosopher''s path.', 'Breakfast'),
       (@p, 7, 'Departure', 'Transfer to Kansai airport.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '6 nights in a 3-star machiya-style hotel with breakfast'),
       (@p, '7-day local transport passes and airport transfers'),
       (@p, 'Tea ceremony and craft workshop fees'),
       (@p, 'English-speaking cultural guide on touring days');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'International airfare (land-only package)'),
       (@p, 'Japan visa fees'),
       (@p, 'Most lunches and dinners'),
       (@p, 'Travel insurance');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Fushimi Inari at dawn'),
       (@p, 'Private tea ceremony'),
       (@p, 'Nara day trip');

-- ---------------------------------------------------------------- Singapore
SET @singapore := (SELECT id FROM destinations WHERE slug = 'singapore');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('singapore-family-fun-5d', 'Singapore Family Fun (Land Only)', @singapore, 'FAMILY',
        'Five land-only days with Sentosa, Gardens by the Bay and the Night Safari.',
        'A family-friendly land-only package with an MRT card, zoo and Night Safari combo, Sentosa cable car and Gardens by the Bay domes. International airfare is NOT included.',
        5, 4, 7450000, 'INR', '/images/destinations/singapore.jpg', 1, 0, 1, 1, 1, 1, 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Singapore', 'Airport transfer, check in and evening at Marina Bay waterfront.', ''),
       (@p, 2, 'City highlights', 'Civic district, Little India, Chinatown and the Botanic Gardens.', 'Breakfast'),
       (@p, 3, 'Sentosa day', 'Cable car, S.E.A. Aquarium and Wings of Time show.', 'Breakfast'),
       (@p, 4, 'Zoo and Night Safari', 'Day at Singapore Zoo followed by the Night Safari tram.', 'Breakfast'),
       (@p, 5, 'Departure', 'Transfer to Changi airport.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '4 nights in a 4-star hotel with breakfast'),
       (@p, 'Airport transfers and MRT tourist cards'),
       (@p, 'Sentosa cable car, aquarium and show tickets'),
       (@p, 'Zoo and Night Safari combo tickets');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'International airfare (land-only package)'),
       (@p, 'Singapore visa fees'),
       (@p, 'Lunch, dinner and personal expenses');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Sentosa island day'),
       (@p, 'Night Safari'),
       (@p, 'Gardens by the Bay');

-- ---------------------------------------------------------------- Dubai
SET @dubai := (SELECT id FROM destinations WHERE slug = 'dubai');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('dubai-skyline-desert-5d', 'Dubai Skyline & Desert (Land Only)', @dubai, 'FAMILY',
        'Five land-only days with a dune safari, Burj Khalifa and old Dubai souks.',
        'Land-only Dubai break with a red-dune safari and barbecue dinner, Burj Khalifa Level 124 tickets and a Deira creek abra ride. International airfare is NOT included.',
        5, 4, 6790000, 'INR', '/images/destinations/dubai.jpg', 1, 0, 1, 1, 1, 1, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Dubai', 'Airport transfer and evening Dubai Marina walk.', ''),
       (@p, 2, 'City tour and Burj Khalifa', 'Jumeirah mosque, museum and Level 124 observation deck at sunset.', 'Breakfast'),
       (@p, 3, 'Desert safari', 'Red-dune drive, camel ride and a desert camp barbecue dinner.', 'Breakfast, dinner'),
       (@p, 4, 'Old Dubai and Dubai Frame', 'Souks, abra crossing and the Dubai Frame.', 'Breakfast'),
       (@p, 5, 'Departure', 'Transfer to the airport.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '4 nights in a 4-star hotel with breakfast'),
       (@p, 'Desert safari with dinner and transfers'),
       (@p, 'Burj Khalifa Level 124 tickets'),
       (@p, 'City tour and airport transfers');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'International airfare (land-only package)'),
       (@p, 'UAE visa fees'),
       (@p, 'Lunch and personal expenses');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'Sunset at Burj Khalifa'),
       (@p, 'Red-dune desert safari'),
       (@p, 'Deira souks and abra');

-- ---------------------------------------------------------------- Darjeeling
SET @darjeeling := (SELECT id FROM destinations WHERE slug = 'darjeeling');
INSERT INTO holiday_packages (slug, title, destination_id, category, summary, description, duration_days, duration_nights,
                              base_price_minor, currency, image_url, land_only, flights_included, hotels_included,
                              transport_included, meals_included, active, featured, created_at, updated_at)
VALUES ('darjeeling-tea-train-5d', 'Darjeeling Tea & Toy Train', @darjeeling, 'CULTURAL',
        'Five days of tea estates, Kanchenjunga sunrises and the UNESCO Darjeeling Himalayan Railway.',
        'Includes a first-flush tea estate tour, Tiger Hill sunrise (weather permitting), the toy train joyride and a day trip to Gangtok''s Rumtek monastery.',
        5, 4, 2890000, 'INR', '/images/destinations/darjeeling.jpg', 1, 0, 1, 1, 1, 1, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
SET @p := LAST_INSERT_ID();
INSERT INTO package_itinerary (package_id, day_number, title, description, meals)
VALUES (@p, 1, 'Arrive Bagdogra / NJP, transfer', 'Scenic 3-hour drive to Darjeeling and an evening on the Mall.', 'Dinner'),
       (@p, 2, 'Toy train and tea estate', 'Darjeeling Himalayan Railway joyride to Ghum, then a working tea estate tour.', 'Breakfast'),
       (@p, 3, 'Tiger Hill and monasteries', 'Sunrise at Tiger Hill, then Yiga Choeling and Bhutia Busty monasteries.', 'Breakfast'),
       (@p, 4, 'Rumtek day trip', 'Drive to Gangtok''s Rumtek monastery and a Ropeway ride on return.', 'Breakfast'),
       (@p, 5, 'Departure', 'Transfer back to Bagdogra / NJP.', 'Breakfast');
INSERT INTO package_inclusions (package_id, line)
VALUES (@p, '4 nights in a 3-star hotel with breakfast and dinner'),
       (@p, 'Darjeeling Himalayan Railway toy train tickets'),
       (@p, 'Tea estate tour with tasting'),
       (@p, 'All transfers and sightseeing by private car');
INSERT INTO package_exclusions (package_id, line)
VALUES (@p, 'Travel to and from Bagdogra / NJP'),
       (@p, 'Gangtok permits, if required'),
       (@p, 'Lunch and personal expenses');
INSERT INTO package_highlights (package_id, line)
VALUES (@p, 'UNESCO toy train joyride'),
       (@p, 'Tiger Hill Kanchenjunga sunrise'),
       (@p, 'First-flush tea tasting');
