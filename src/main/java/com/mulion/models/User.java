package com.mulion.models;

public interface User {
    String getUserToken();

    long getStaffId();

    String getLoginAndPassword();

    void setUserToken(String userToken);

    int getCash();

    void addCash(int cash);
}
