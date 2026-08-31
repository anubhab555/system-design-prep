package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model;

import java.util.*;

public class Screen {
    private final String id;
    private final List<Seat> seats;
    
    public Screen(String id) {
        this.id = id;
        this.seats = new ArrayList<>();
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
    }

    public String getId() {
        return id;
    }

    public List<Seat> getSeats() {
        return seats;
    }
}
