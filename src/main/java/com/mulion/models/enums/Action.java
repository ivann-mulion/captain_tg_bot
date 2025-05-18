package com.mulion.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Action {
    INACTIVE(UserRole.CAPTAIN),
    REGISTRATION(UserRole.CAPTAIN),
    CAPTAIN_MENU(UserRole.CAPTAIN),
    CHANGE_BOAT(UserRole.CAPTAIN),
    MANAGER_MENU(UserRole.MANAGER),
    ADD_BOAT_TO_CAPTAIN(UserRole.MANAGER),
    REMOVE_CAPTAINS_BOAT(UserRole.MANAGER),
    ADMIN_MENU(UserRole.ADMIN),
    ADD_BOAT_IN_SYSTEM(UserRole.ADMIN),
    SET_USERS_ROLE(UserRole.ADMIN);

    private final UserRole access;
}
