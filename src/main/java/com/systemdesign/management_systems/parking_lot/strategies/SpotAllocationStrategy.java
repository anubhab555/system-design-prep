package com.systemdesign.management_systems.parking_lot.strategies;

import java.util.List;
import com.systemdesign.management_systems.parking_lot.models.ParkingSpot;
import com.systemdesign.management_systems.parking_lot.models.ParkingFloor;
import com.systemdesign.management_systems.parking_lot.enums.VehicleSize;

public interface SpotAllocationStrategy {
    ParkingSpot findSpot(List<ParkingFloor> floors, VehicleSize size);
}
