package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model;

import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.enums.SeatStatus;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.enums.SeatType;

public class Seat {
    private final String id;
    private final SeatType seatType;
    private SeatStatus seatStatus;
    private final int row;
    private final int col;


    public Seat(String id, int row, int col, SeatType seatType) {
        this.id = id;
        this.seatType = seatType;
        this.seatStatus = SeatStatus.AVAILABLE;
        this.row = row;
        this.col = col;
    }

    public String getId() {
        return id;
    }
    public SeatType getSeatType() {
        return seatType;
    }
    public SeatStatus getSeatStatus() {
        return seatStatus;
    }
    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }

    public void setSeatStatus(SeatStatus seatStatus) {
        this.seatStatus = seatStatus;
    }
}
