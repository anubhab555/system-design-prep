package com.systemdesign.management_systems.parking_lot;

import java.util.*;

import com.systemdesign.management_systems.parking_lot.enums.VehicleSize;
import com.systemdesign.management_systems.parking_lot.models.*;
import com.systemdesign.management_systems.parking_lot.strategies.*;
import com.systemdesign.management_systems.parking_lot.exception.*;

public class ParkingLotDemo {
    public static void main(String[] args) {
        // Build two floors with a mix of spot sizes
        Map<VehicleSize, Integer> floor1Spots = new HashMap<>();
        floor1Spots.put(VehicleSize.SMALL, 2);
        floor1Spots.put(VehicleSize.MEDIUM, 2);
        floor1Spots.put(VehicleSize.LARGE, 1);

        Map<VehicleSize, Integer> floor2Spots = new HashMap<>();
        floor2Spots.put(VehicleSize.SMALL, 1);
        floor2Spots.put(VehicleSize.MEDIUM, 1);
        floor2Spots.put(VehicleSize.LARGE, 1);

        List<ParkingFloor> floors = List.of(
            new ParkingFloor(1, floor1Spots),
            new ParkingFloor(2, floor2Spots)
        );

        // Initialize the singleton with strategies
        ParkingLot lot = ParkingLot.getInstance();
        lot.initialize(floors, new HourlyFeeStrategy(10.0), new NearestFirstStrategy());

        lot.displayAvailability();

        // === Scenario 1: Park a few vehicles (nearest-first) ===
        System.out.println("========== SCENARIO 1: Park Vehicles (Nearest First) ==========");
        ParkingTicket bikeTicket = lot.parkVehicle(new Bike("KA-01-1111"));
        ParkingTicket carTicket = lot.parkVehicle(new Car("KA-02-2222"));
        lot.parkVehicle(new Truck("KA-03-3333"));

        lot.displayAvailability();

        // === Scenario 2: Switch to best-fit allocation ===
        System.out.println("========== SCENARIO 2: Park With Best Fit ==========");
        lot.setAllocationStrategy(new BestFitStrategy());
        lot.parkVehicle(new Car("KA-04-4444"));

        // === Scenario 3: Unpark and pay ===
        System.out.println("\n========== SCENARIO 3: Unpark Vehicles ==========");
        lot.unparkVehicle(bikeTicket.getTicketId());
        lot.unparkVehicle(carTicket.getTicketId());

        lot.displayAvailability();

        // === Scenario 4: Fill the large spots, then fail to park a truck ===
        System.out.println("========== SCENARIO 4: No Spot Available ==========");
        try {
            lot.setAllocationStrategy(new NearestFirstStrategy());
            lot.parkVehicle(new Truck("KA-05-5555"));  // remaining large spot
            lot.parkVehicle(new Truck("KA-06-6666"));  // should fail
        } catch (ParkingException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }

        // Clean up the singleton so repeated runs start fresh
        ParkingLot.resetInstance();
    }
}
