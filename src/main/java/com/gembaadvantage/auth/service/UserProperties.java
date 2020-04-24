package com.gembaadvantage.auth.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("users")
public class UserProperties {
    private List<DummyUser> userList = new ArrayList<>();

    public List<DummyUser> getUserList() {
        return userList;
    }

    public void setUserList(List<DummyUser> userList) {
        this.userList = userList;
    }
}
