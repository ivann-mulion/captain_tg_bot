package com.mulion.data_base.listeners;

import com.mulion.models.User;
import com.mulion.yclients.services.YCUserService;
import jakarta.persistence.PostLoad;

public class UserListener {
    @PostLoad
    public void auth(User user) {
        if (user.getUserToken() == null
                && user.getLogin() != null
                && user.getPassword() != null) {
            YCUserService.authorization(user);
        }
    }
}
