package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model;

public class City{
    private final String id;
    private final String name;

    public City(String id, String name){
        this.id = id;
        this.name = name;
    }

    

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}