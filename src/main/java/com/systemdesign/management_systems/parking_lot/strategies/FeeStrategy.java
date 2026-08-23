package com.systemdesign.management_systems.parking_lot.strategies;

import com.systemdesign.management_systems.parking_lot.models.ParkingTicket;

public interface FeeStrategy {
    double calculateFee(ParkingTicket ticket);
}
