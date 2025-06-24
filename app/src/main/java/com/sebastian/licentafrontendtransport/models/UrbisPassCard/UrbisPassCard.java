package com.sebastian.licentafrontendtransport.models.UrbisPassCard;

import com.google.gson.annotations.SerializedName;
import com.sebastian.licentafrontendtransport.models.Payment.Card;

import java.io.Serializable;

public class UrbisPassCard implements Serializable {

    private String fullName;

    private String cnp;


    private int numberOfDays;

    private boolean isStudent;

    private Card cardPayment;
    private boolean keepCard;

    public UrbisPassCard(String fullName, String cnp, int numberOfDays, boolean isStudent, Card cardPayment, boolean keepCard) {
        this.cnp = cnp;

        this.isStudent = isStudent;
        this.fullName = fullName;
        this.numberOfDays = numberOfDays;
        this.cardPayment = cardPayment;
        this.keepCard = keepCard;



    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public boolean isStudent() {
        return isStudent;
    }

    public void setStudent(boolean student) {
        isStudent = student;
    }
    public Card getCardPayment() {
        return cardPayment;
    }
    public void setCardPayment(Card cardPayment) {
        this.cardPayment = cardPayment;
    }
}
