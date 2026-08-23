package com.systemdesign.management_systems.parking_lot.strategies;

import com.systemdesign.management_systems.parking_lot.models.ParkingTicket;

public class FlatRateFeeStrategy implements FeeStrategy {
    private final double flatRate;

    public FlatRateFeeStrategy(double flatRate) {
        this.flatRate = flatRate;
    }

    @Override
    public double calculateFee(ParkingTicket ticket) {
        return flatRate;
    }
}