package com.systemdesign.management_systems.parking_lot.models;

import com.systemdesign.management_systems.parking_lot.enums.VehicleSize;

public class Bike extends Vehicle {
    public Bike(String licensePlate) {
        super(licensePlate, VehicleSize.SMALL);
    }
}