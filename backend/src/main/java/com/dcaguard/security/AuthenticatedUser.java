package com.dcaguard.security;

import com.dcaguard.entity.User;

public class AuthenticatedUser {

    private final User user;

    public AuthenticatedUser(User user) {
        this.user = user;
    }

    public User getUser() { return user; }
    public Long getUserId() { return user.getId(); }
    public String getFirebaseUid() { return user.getFirebaseUid(); }
}
