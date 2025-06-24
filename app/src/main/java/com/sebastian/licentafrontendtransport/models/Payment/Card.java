package com.sebastian.licentafrontendtransport.models.Payment;


import java.io.Serializable;
import java.util.Objects;

public class Card implements Serializable {
    private String stripePaymentMethodId;
    private String cardName;
    private String brand;
    private String last4;
    private int expMonth;
    private int expYear;

    public Card() {
    }

    public Card(
                String stripePaymentMethodId,
                String cardName,
                String userId,
                String brand,
                String last4,
                int expMonth,
                int expYear) {

        this.stripePaymentMethodId = stripePaymentMethodId;
        this.brand = brand;
        this.last4 = last4;
        this.cardName = cardName;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getStripePaymentMethodId() {
        return stripePaymentMethodId;
    }

    public void setStripePaymentMethodId(String stripePaymentMethodId) {
        this.stripePaymentMethodId = stripePaymentMethodId;
    }



    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getLast4() {
        return last4;
    }

    public void setLast4(String last4) {
        this.last4 = last4;
    }

    public int getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(int expMonth) {
        this.expMonth = expMonth;
    }

    public int getExpYear() {
        return expYear;
    }

    public void setExpYear(int expYear) {
        this.expYear = expYear;
    }


    @Override
    public int hashCode() {
        return Objects.hash(stripePaymentMethodId, brand, last4, expMonth, expYear);
    }

}