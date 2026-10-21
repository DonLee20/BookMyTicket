package com.bookmyticket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed application configuration. Values come from environment variables and
 * application.yml; secrets are never exposed to the frontend.
 */
@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Demo demo = new Demo();
    private Admin admin = new Admin();
    private Booking booking = new Booking();
    private Urls urls = new Urls();
    private Payment payment = new Payment();
    private Providers providers = new Providers();

    @Data
    public static class Demo {
        private int busDaysAhead = 30;
        private boolean autoSeedInventory = true;
    }

    @Data
    public static class Admin {
        private String email = "";
        private String name = "Platform Administrator";
        private String password = "";
    }

    @Data
    public static class Booking {
        private int holdMinutes = 20;
        private int holidayCapacityPerDeparture = 24;
    }

    @Data
    public static class Urls {
        private String irctcOfficial = "https://www.irctc.co.in/nget/train-search";
        private String mtcOfficial = "https://www.mtcbus.tn.gov.in/";
    }

    @Data
    public static class Payment {
        private String provider = "demo";
        private String webhookSecret = "dev_webhook_secret_change_me";
    }

    @Data
    public static class Providers {
        private BusProvider bus = new BusProvider();
        private FlightProvider flight = new FlightProvider();
        private Provider railway = new Provider();
        private Provider mtc = new Provider();
        private PaymentProvider payment = new PaymentProvider();
    }

    @Data
    public static class Provider {
        /** DEMO | LIVE | EXTERNAL_HANDOFF | UNAVAILABLE */
        private String mode = "DEMO";
    }

    /**
     * Backend-only credentials for a future authorized live bus adapter. These
     * values are never serialized or exposed by the API; search responses only
     * expose whether credentials are present.
     */
    @Data
    public static class BusProvider extends Provider {
        private String liveProvider = "";
        private String liveBaseUrl = "";
        private String liveApiKey = "";
        private String liveApiSecret = "";
    }

    @Data
    public static class FlightProvider extends Provider {
        private String amadeusClientId = "";
        private String amadeusClientSecret = "";
    }

    @Data
    public static class PaymentProvider extends Provider {
        private String razorpayKeyId = "";
        private String razorpayKeySecret = "";
    }
}
