package com.mulion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mulion.constants.HttpStatusCodes;
import com.mulion.models.User;
import com.mulion.models.impl.UserImpl;

import javax.security.auth.login.FailedLoginException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.mulion.constants.Config.httpClient;
import static com.mulion.constants.Config.httpRequests;

public class UserService {
    public static void authorization(User user) throws FailedLoginException {
        HttpRequest request = httpRequests.getAuthRequest(user);
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpStatusCodes.OK) {
                throw new FailedLoginException();
            }

            JsonMapper mapper = new JsonMapper();
            JsonNode node = mapper.readTree(response.body());

            user.setUserToken(node.path("data").path("user_token").asText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static User getUser(long userId) {
        return new UserImpl();
    }

    private UserService() {
    }
}
