package com.mulion.telegram_bot_application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DateMenu {
    TODAY("today"),
    YESTERDAY("yesterday"),
    CUSTOM_DATE("custom_date"),
    MENU("menu"),
    NOT_DATE(null);

    private final String action;

    public static DateMenu getDateFromString(String string) {
        try {
            return valueOf(string.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NOT_DATE;
        }
    }
}