package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model;

public class Movie{
    private final String title;
    private final int durationInMinutes;
    private final String id;
    
    public Movie(String id, String title, int durationInMinutes) {
        this.title = title;
        this.durationInMinutes = durationInMinutes;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public String getId() {
        return id;
    }
    
}