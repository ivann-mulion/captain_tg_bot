package com.mulion.models;

import com.mulion.models.enums.Action;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Steps {
    private Action action;
    private int step;

    public Steps() {
        action = Action.REGISTRATION;
        step = 0;
    }

    public int nextStep() {
        return step++;
    }

    public void restartAction() {
        step = 0;
    }

    public void inactivate() {
        action = Action.INACTIVE;
        step = 0;
    }
}
