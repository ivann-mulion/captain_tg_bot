package com.mulion.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Date {
    TODAY("today"),
    YESTERDAY("yesterday"),
    CUSTOM_DATE("custom_date"),
    NOT_DATE(null);

    private final String day;

    public static Date getDateFromString(String string) {
        try {
            return valueOf(string.toUpperCase());
        } catch (IllegalArgumentException _) {
            return NOT_DATE;
        }
    }
}