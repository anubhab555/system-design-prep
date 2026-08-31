package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.strategies.pricing;

import java.util.*;

import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.enums.SeatType;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Seat;

public interface PricingStrategy {
    double calculatePrice(List<Seat> seats, Map<SeatType, Double> seatPrices);
}
