package com.mulion.models;

import com.mulion.models.enums.UserRegistrationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Steps {
    @Column(name = "registration_status")
    private UserRegistrationStatus registrationStatus;

    public Steps() {
        registrationStatus = UserRegistrationStatus.START;
    }
}
