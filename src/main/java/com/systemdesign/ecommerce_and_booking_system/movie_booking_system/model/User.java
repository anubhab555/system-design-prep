package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model;

import java.util.UUID;

public class User {
    private final String userId;
    private final String name;
    private final String email;
    
    public User(String name, String email) {
        this.userId = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

}
