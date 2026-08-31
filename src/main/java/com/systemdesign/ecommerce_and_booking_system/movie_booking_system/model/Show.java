package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model;

import java.util.*;

import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.enums.SeatType;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.strategies.pricing.PricingStrategy;

import java.time.LocalDateTime;

public class Show {
    private final String id;
    private final Movie movie;
    private final Screen screen;
    private final LocalDateTime startTime;
    private final Map<SeatType, Double> seatPrices;
    private final PricingStrategy pricingStrategy;
    private final Object lock = new Object();
    

    public Show(String id, Movie movie, Screen screen, LocalDateTime startTime, Map<SeatType, Double> seatPrices,
            PricingStrategy pricingStrategy) {
        this.id = id;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.seatPrices = seatPrices;
        this.pricingStrategy = pricingStrategy;
    }


    public String getId() {
        return id;
    }


    public Movie getMovie() {
        return movie;
    }


    public Screen getScreen() {
        return screen;
    }


    public LocalDateTime getStartTime() {
        return startTime;
    }


    public Map<SeatType, Double> getSeatPrices() {
        return seatPrices;
    }


    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }


    public Object getLock() {
        return lock;
    }
}
