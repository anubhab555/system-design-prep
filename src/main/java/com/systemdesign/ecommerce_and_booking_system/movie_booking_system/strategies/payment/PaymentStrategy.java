package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.strategies.payment;

import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Payment;

public interface PaymentStrategy {
    Payment pay(double amount);
    void refund(Payment payment);
}
