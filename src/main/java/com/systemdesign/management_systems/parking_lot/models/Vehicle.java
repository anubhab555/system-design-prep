package com.systemdesign.management_systems.parking_lot.models;

import com.systemdesign.management_systems.parking_lot.enums.VehicleSize;

public class Vehicle {
    public final String licensePlate;
    public final VehicleSize size;

    protected Vehicle(String licensePlate, VehicleSize size) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
        this.licensePlate = licensePlate;
        this.size = size;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleSize getSize() {
        return size;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + licensePlate + "]";
    }
}
