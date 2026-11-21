package com.gps.cs.entity;

public enum Role {
    CLIENT, ADMIN, MANAGER, PARTNER, SALES, MARKETING;

    public boolean isStaff() {
        return this != CLIENT && this != ADMIN;
    }
}
