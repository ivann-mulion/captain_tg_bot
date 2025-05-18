package com.mulion.telegram_bot_application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DateMenu {
    TODAY,
    YESTERDAY,
    CUSTOM_DATE,
    CAPTAIN_MENU,
    MANAGER_MENU,
    ADMIN_MENU
}