package com.mulion.yclients.services;

import com.mulion.constants.HttpStatusCodes;
import com.mulion.models.User;

import javax.security.auth.login.FailedLoginException;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.mulion.constants.Config.httpClient;
import static com.mulion.constants.Config.httpRequests;

public class YCBoatService {
    public static boolean isBoat(User user, Long staffId) {
        HttpRequest request = httpRequests.getStaffRequest(user, staffId);
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpStatusCodes.OK) {
                throw new FailedLoginException();
            }
        } catch (IOException | FailedLoginException _) {
            return false;
        }  catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }
}
