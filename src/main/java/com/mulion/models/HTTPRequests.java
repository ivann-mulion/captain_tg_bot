package com.mulion.models;

import com.mulion.models.responces.DataRecord;

import java.net.http.HttpRequest;
import java.time.LocalDate;

public interface HTTPRequests {
    HttpRequest getAuthRequest(User user);

    HttpRequest getRecordsRequest(User user, LocalDate date);

    HttpRequest getRecordsRequest(User user, LocalDate start, LocalDate end, int page);

    HttpRequest getVisitDetailsRequest(User user, DataRecord dataRecord);
}
