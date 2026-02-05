package com.seolstudy.backend.domain.user.entity;

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
