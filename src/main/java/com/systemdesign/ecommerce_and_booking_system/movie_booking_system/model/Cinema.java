package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model;

import java.util.*;

public class Cinema {
    private final String name;
    private final List<Screen> screens;
    private final String id;
    private final City city;

    public Cinema(String name, String id, City city, List<Screen> screens) {
        this.name = name;
        this.id = id;
        this.city = city;
        this.screens = screens;
    }

    public String getName() {
        return name;
    }

    public List<Screen> getScreens() {
        return screens;
    }

    public String getId() {
        return id;
    }

    public City getCity() {
        return city;
    }
}
