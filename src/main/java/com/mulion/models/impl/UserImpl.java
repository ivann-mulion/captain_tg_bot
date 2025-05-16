package com.mulion.models.impl;

import com.mulion.models.User;
import com.mulion.services.ConfigService;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class UserImpl implements User {
    private String userToken = ConfigService.getProperty("yc.user_token");
    private long staffId = Long.parseLong(ConfigService.getProperty("yc.staff_token"));
    private final String login = ConfigService.getProperty("yc.login");
    private final String password = ConfigService.getProperty("yc.password");
    private int cash;

    @Override
    public String getLoginAndPassword() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("login", login)
                .append("password", password)
                .toString();
    }

    @Override
    public void addCash(int cash) {
        this.cash += cash;
    }
}
