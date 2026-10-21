package com.bookmyticket.catalog;

public enum PackageCategory {
    FAMILY,
    HONEYMOON,
    ADVENTURE,
    CULTURAL,
    WILDLIFE,
    WEEKEND,
    CUSTOM;

    public String getLabel() {
        String lower = name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
