package com.sebastian.licentafrontendtransport.models.Payment;

//nu ne trebuie intodeauna tot ce este in body
public class PaymentMethodDto {
    private String paymentMethod;
    private boolean saving;

    public PaymentMethodDto(String paymentMethod, boolean saving) {
        this.paymentMethod = paymentMethod;
        this.saving = saving;

    }

    public String getId() {
        return paymentMethod;
    }
}
