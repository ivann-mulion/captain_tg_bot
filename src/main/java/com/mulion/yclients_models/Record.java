package com.mulion.yclients_models;

import java.time.LocalDate;
import java.time.LocalTime;

public interface Record {
    LocalDate getDate();

    LocalTime getStartTime();

    int getPrepayment();

    int getAcquiring();

    int getCash();

    int getLength();
}
