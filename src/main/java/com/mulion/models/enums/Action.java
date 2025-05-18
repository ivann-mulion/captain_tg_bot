package com.mulion.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Action {
    INACTIVE(UserRole.CAPTAIN),
    REGISTRATION(UserRole.CAPTAIN),
    MENU(UserRole.ADMIN),
    ADD_BOAT_IN_SYSTEM(UserRole.ADMIN),
    ADD_BOAT_TO_CAPTAIN(UserRole.ADMIN),
    REMOVE_CAPTAINS_BOAT(UserRole.ADMIN),
    SET_USERS_ROLE(UserRole.ADMIN);

    private final UserRole access;
}
