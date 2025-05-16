package com.mulion.yclients_models.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mulion.constants.HttpStatusCodes;
import com.mulion.models.User;

import javax.security.auth.login.FailedLoginException;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.mulion.constants.Config.httpClient;
import static com.mulion.constants.Config.httpRequests;

public class YCUserService {
    public static boolean authorization(User user) {
        HttpRequest request = httpRequests.getAuthRequest(user);
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpStatusCodes.OK) {
                throw new FailedLoginException();
            }

            JsonMapper mapper = new JsonMapper();
            JsonNode node = mapper.readTree(response.body());

            user.setUserToken(node.path("data").path("user_token").asText());
        } catch (IOException | FailedLoginException e) {
            return false;
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    private YCUserService() {
    }
}
