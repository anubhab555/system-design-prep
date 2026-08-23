package com.systemdesign.management_systems.parking_lot.models;

import com.systemdesign.management_systems.parking_lot.enums.VehicleSize;

public class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate, VehicleSize.LARGE);
    }
}
