package com.seolstudy.backend.domain.user;

public enum UserRole {
    MENTOR,
    MENTEE;

    private UserRole role;

    UserRole() {
    }

    public UserRole getRole() {
        return role;
    }
}
