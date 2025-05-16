package com.mulion.models;

import java.time.LocalDate;
import java.time.LocalTime;

public interface Record {
    LocalDate getDate();

    LocalTime getStartTime();

    LocalTime getEndTime();

    String getPaymentInfo();

    boolean isDone();

    int getPrepayment();

    int getAcquiring();

    int getCash();

    int getLength();
}
