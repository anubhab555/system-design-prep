package com.systemdesign.management_systems.parking_lot.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.systemdesign.management_systems.parking_lot.enums.VehicleSize;

public class ParkingFloor{
    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, Map<VehicleSize, Integer> spotCounts){
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
        initializeSpots(spotCounts);
    }

    private void initializeSpots(Map<VehicleSize, Integer> spotCounts){
        for(VehicleSize size : VehicleSize.values()){
            int count = spotCounts.getOrDefault(size, 0);
            String prefix = getSizePrefix(size);
            for(int i=1; i<=count; i++){
                String spotId = String.format("F%d-%s%03d", floorNumber, prefix, i);
                spots.add(new ParkingSpot(spotId, size));
            }
        }
    }

    private String getSizePrefix(VehicleSize size){
        switch(size){
            case SMALL : return "S";
            case MEDIUM : return "M";
            case LARGE : return "L";
            default : return "X";
        }
    }

    public ParkingSpot findAvailableSpot(VehicleSize size){
        for(ParkingSpot spot : spots){
            if(spot.isAvailable() && spot.canFitVehicle(size)){
                return spot;
            }
        }

        return null;
    }

    public int getFloorNumber(){
        return floorNumber;
    }

    public List<ParkingSpot> getSpots(){
        return Collections.unmodifiableList(spots);
    }
}