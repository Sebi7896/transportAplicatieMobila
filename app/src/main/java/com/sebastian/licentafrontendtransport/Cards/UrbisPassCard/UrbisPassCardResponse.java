package com.sebastian.licentafrontendtransport.Cards.UrbisPassCard;

public class UrbisPassCardResponse {
    private String message;
    public UrbisPassCardResponse(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}