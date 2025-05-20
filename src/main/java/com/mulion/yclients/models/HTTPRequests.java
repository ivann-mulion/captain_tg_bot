package com.mulion.yclients.models;

import com.mulion.models.User;
import com.mulion.yclients.models.responses.DataRecord;

import java.net.http.HttpRequest;
import java.time.LocalDate;

public interface HTTPRequests {
    HttpRequest getAuthRequest(User user);

    HttpRequest getRecordsRequest(User user, LocalDate date);

    HttpRequest getRecordsRequest(User user, LocalDate start, LocalDate end, int page);

    HttpRequest getVisitDetailsRequest(User user, DataRecord dataRecord);

    HttpRequest getStaffRequest(User user, Long staffId);
}
