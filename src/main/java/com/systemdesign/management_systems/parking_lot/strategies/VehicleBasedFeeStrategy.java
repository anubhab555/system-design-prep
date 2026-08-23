package com.systemdesign.management_systems.parking_lot.strategies;

import java.util.Map;
import com.systemdesign.management_systems.parking_lot.enums.VehicleSize;
import com.systemdesign.management_systems.parking_lot.models.ParkingTicket;

class VehicleBasedFeeStrategy implements FeeStrategy {
    private final Map<VehicleSize, Double> ratesPerHour;

    public VehicleBasedFeeStrategy(Map<VehicleSize, Double> ratesPerHour) {
        this.ratesPerHour = ratesPerHour;
    }

    @Override
    public double calculateFee(ParkingTicket ticket) {
        VehicleSize size = ticket.getVehicle().getSize();
        double rate = ratesPerHour.getOrDefault(size, 0.0);
        return ticket.getDurationInHours() * rate;
    }
}
