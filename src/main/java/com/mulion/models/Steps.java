package com.mulion.models;

import com.mulion.enums.RegistrationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Steps {
    @Column(name = "registration_status")
    private RegistrationStatus registrationStatus;

    public Steps() {
        registrationStatus = RegistrationStatus.START;
    }
}
