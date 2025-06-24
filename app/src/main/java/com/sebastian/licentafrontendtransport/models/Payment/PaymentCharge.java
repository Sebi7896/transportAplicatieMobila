package com.sebastian.licentafrontendtransport.models.Payment;

public class PaymentCharge {
    private String paymentId;
    private int amount;
    public PaymentCharge(String paymentId, int amount) {
        this.paymentId = paymentId;
        this.amount = amount;
    }
    public String getPaymentId() {
        return paymentId;
    }
    public int getAmount() {
        return amount;
    }
}

