package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model;

import java.util.*;

public class Booking {
    private final String id;
    private final User user;
    private final Show show;
    private final List<Seat> seats;
    private final double amount;
    private final Payment payment;

    public Booking(User user, Show show, List<Seat> seats, double amount, Payment payment) {
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.show = show;
        this.seats = seats;
        this.amount = amount;
        this.payment = payment;
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Show getShow() {
        return show;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public double getAmount() {
        return amount;
    }

    public Payment getPayment() {
        return payment;
    }
}
