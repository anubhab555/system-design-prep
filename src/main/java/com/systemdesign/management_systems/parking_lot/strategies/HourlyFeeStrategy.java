package com.systemdesign.management_systems.parking_lot.strategies;

import com.systemdesign.management_systems.parking_lot.models.ParkingTicket;

public class HourlyFeeStrategy implements FeeStrategy {
    private final double ratePerHour;

    public HourlyFeeStrategy(double ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

    @Override
    public double calculateFee(ParkingTicket ticket) {
        return ticket.getDurationInHours() * ratePerHour;
    }
}
