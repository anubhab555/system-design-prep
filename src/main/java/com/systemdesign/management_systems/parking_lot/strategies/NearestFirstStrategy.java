package com.systemdesign.management_systems.parking_lot.strategies;

import com.systemdesign.management_systems.parking_lot.models.ParkingFloor;
import com.systemdesign.management_systems.parking_lot.models.ParkingSpot;

import com.systemdesign.management_systems.parking_lot.enums.VehicleSize;

import java.util.List;

public class NearestFirstStrategy implements SpotAllocationStrategy {
    @Override
    public ParkingSpot findSpot(List<ParkingFloor> floors, VehicleSize size) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(size);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }
}
