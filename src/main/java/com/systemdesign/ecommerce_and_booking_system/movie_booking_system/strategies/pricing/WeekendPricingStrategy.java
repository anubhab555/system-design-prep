package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.strategies.pricing;

import java.util.List;
import java.util.Map;

import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.enums.SeatType;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Seat;

public class WeekendPricingStrategy implements PricingStrategy{
    private static final double WEEKEND_SURCHARGE = 1.2; // 20% surcharge

    @Override
    public double calculatePrice(List<Seat> seats, Map<SeatType, Double> seatPrices) {
        double basePrice = seats.stream().mapToDouble(seat -> seatPrices.get(seat.getSeatType())).sum();
        return basePrice * WEEKEND_SURCHARGE;
    }
}
