package com.mulion.yclients_models.impl;

import com.mulion.constants.Config;
import com.mulion.yclients_models.HTTPRequests;
import com.mulion.models.User;
import com.mulion.yclients_models.responses.DataRecord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HTTPRequestsImpl implements HTTPRequests {

    public static final String AUTH_URI = "/auth";
    public static final String RECORDS_URI = "/records";
    public static final String VISIT_URI = "/visit/details";
    public static final String ACCEPT_HEADER = "application/vnd.api.v2+json";
    public static final String CONTENT_TYPE = "application/json";
    public static final String AUTHORIZATION = "Authorization";

    @Override
    public HttpRequest getAuthRequest(User user) {
        URI uri = URI.create(Config.BASE_URL + AUTH_URI);
        return getBaseRequest(uri)
                .header(AUTHORIZATION, String.format("Bearer %s", Config.PARTNER_TOKEN))
                .POST(HttpRequest.BodyPublishers.ofString(user.getLoginAndPassword()))
                .build();
    }

    @Override
    public HttpRequest getRecordsRequest(User user, LocalDate date) {
        return getRecordsRequest(user, date, date.plusDays(1), 0);
    }

    @Override
    public HttpRequest getRecordsRequest(User user, LocalDate start, LocalDate end, int page) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        URI uri = URI.create(Config.BASE_URL + RECORDS_URI +
                String.format("/%d?start_date=%s&end_date=%s&staff_id=%d",
                        Config.COMPANY_ID, start.format(formatter), end.format(formatter), user.getStaffId()));
        if (page > 0) {
            uri = URI.create(uri + "&page=" + page);
        }
        return getBaseRequest(uri)
                .header(AUTHORIZATION, String.format("Bearer %s, User %s",
                        Config.PARTNER_TOKEN,
                        user.getUserToken()))
                .GET()
                .build();
    }

    @Override
    public HttpRequest getVisitDetailsRequest(User user, DataRecord dataRecord) {
        URI uri = URI.create(Config.BASE_URL + VISIT_URI +
                String.format("/%d/%s/%s", Config.COMPANY_ID, dataRecord.getId(), dataRecord.getVisitId()));
        return getBaseRequest(uri)
                .header(AUTHORIZATION, String.format("Bearer %s, User %s",
                        Config.PARTNER_TOKEN,
                        user.getUserToken()))
                .GET()
                .build();
    }

    private HttpRequest.Builder getBaseRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", ACCEPT_HEADER)
                .header("Content-Type", CONTENT_TYPE);
    }
}
