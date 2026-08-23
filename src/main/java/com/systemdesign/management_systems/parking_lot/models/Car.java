package com.systemdesign.management_systems.parking_lot.models;

import com.systemdesign.management_systems.parking_lot.enums.VehicleSize;

public class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleSize.LARGE);
    }
}
