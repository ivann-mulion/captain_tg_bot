package com.mulion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mulion.constants.HttpStatusCodes;
import com.mulion.models.Record;
import com.mulion.models.User;
import com.mulion.models.impl.RecordImpl;
import com.mulion.models.responces.DataRecord;
import com.mulion.models.responces.PaymentTransactionRecord;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.mulion.constants.Config.yclientDateFormatter;
import static com.mulion.constants.Config.httpClient;
import static com.mulion.constants.Config.httpRequests;
import static com.mulion.constants.Config.startDayTime;

public class RecordService {

    public static List<Record> getRecords(User user, LocalDate date) throws AuthenticationException {
        HttpRequest request = httpRequests.getRecordsRequest(user, date);
        List<Record> result = new ArrayList<>();
        try {
            HttpResponse<String> response = getResponse(request, user);

            List<DataRecord> records = getRecords(response.body());

            for (DataRecord dataRecord : records) {
                LocalDateTime recordDateTime = LocalDateTime.parse(dataRecord.getDate(), yclientDateFormatter);
                if (recordDateTime.isBefore(LocalDateTime.of(date, startDayTime)) ||
                        recordDateTime.toLocalDate().isAfter(date) && !recordDateTime.toLocalTime().isBefore(startDayTime)) {
                    continue;
                }
                HttpRequest visitRequest = httpRequests.getVisitDetailsRequest(user, dataRecord);
                response = getResponse(visitRequest, user);

                result.add(new RecordImpl(dataRecord, getShortTransactions(response.body())));
            }
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private static HttpResponse<String> getResponse(HttpRequest request, User user) throws AuthenticationException, IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkAuthentication(user, response);
        return response;
    }

    private static void checkAuthentication(User user, HttpResponse<String> response) throws AuthenticationException {
        if (response.statusCode() == HttpStatusCodes.UNAUTHORIZED) {
            throw new AuthenticationException(String.format("user %s is unauthenticated", user));
        }
    }

    private static List<PaymentTransactionRecord> getShortTransactions(String response) throws IOException {
        JsonMapper mapper = new JsonMapper();
        mapper.registerModule(new JavaTimeModule());

        JsonNode root = mapper.readTree(response);
        JsonNode transactionsNode = root.path("data").path("payment_transactions");

        return mapper.readerForListOf(PaymentTransactionRecord.class)
                .readValue(transactionsNode);
    }

    private static List<DataRecord> getRecords(String response) throws IOException {
        JsonMapper mapper = new JsonMapper();
        mapper.registerModule(new JavaTimeModule());

        JsonNode root = mapper.readTree(response);
        JsonNode dataArray = root.path("data");

        return mapper.readerForListOf(DataRecord.class).readValue(dataArray);
    }

    private RecordService() {
    }
}
