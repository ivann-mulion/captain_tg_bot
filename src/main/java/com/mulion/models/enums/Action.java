package com.mulion.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Action {
    REGISTRATION(UserRole.CAPTAIN),
    INACTIVE(UserRole.CAPTAIN),
    MENU(UserRole.ADMIN),
    ADD_BOAT(UserRole.ADMIN),
    ADD_BOAT_TO_CAPTAIN(UserRole.ADMIN);

    private final UserRole access;
}
