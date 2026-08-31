package com.systemdesign.ecommerce_and_booking_system.movie_booking_system.strategies.payment;

import java.util.UUID;

import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.enums.PaymentStatus;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Payment;

public class CreditCardPaymentStrategy implements PaymentStrategy{
    private final String cvv;
    private final String cardNumber;

    public CreditCardPaymentStrategy(String cvv, String cardNumber) {
        this.cvv = cvv;
        this.cardNumber = cardNumber;
    }

    @Override
    public Payment pay(double amount){
         System.out.printf("Processing credit card payment of $%.2f%n", amount);
        // Simulate payment gateway interaction
        boolean paymentSuccess = Math.random() > 0.05; // 95% success rate
        return new Payment(
                amount,
                paymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILURE,
                "TXN_" + UUID.randomUUID()
        );
    }

    @Override
    public void refund(Payment payment){
        System.out.printf("Refunding credit card payment of $%.2f (txn %s)%n",
                payment.getAmount(), payment.getTransactionId());
    }
}
