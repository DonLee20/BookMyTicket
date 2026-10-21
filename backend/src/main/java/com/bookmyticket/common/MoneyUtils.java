package com.bookmyticket.common;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    public static String format(long amountMinor, String currency) {
        double amount = amountMinor / 100.0;
        Locale locale = "INR".equalsIgnoreCase(currency) ? Locale.forLanguageTag("en-IN") : Locale.US;
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setMaximumFractionDigits(amountMinor % 100 == 0 ? 0 : 2);
        format.setMinimumFractionDigits(0);
        String formatted = format.format(amount);
        if (!"INR".equalsIgnoreCase(currency)) {
            formatted = formatted + " " + currency;
        }
        return formatted;
    }
}
