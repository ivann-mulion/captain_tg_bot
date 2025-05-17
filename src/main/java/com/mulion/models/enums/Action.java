package com.mulion.models.enums;

import lombok.Getter;

@Getter
public enum Action {
    REGISTRATION(UserRole.CAPTAIN),
    INACTIVE(UserRole.CAPTAIN),
    ADD_BOAT(UserRole.ADMIN),
    ADD_BOAT_TO_CAPTAIN(UserRole.ADMIN);

    private final UserRole access;

    Action(UserRole role) {
        access = role;
    }
}
