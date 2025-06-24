package com.sebastian.licentafrontendtransport.models.UrbisPassCard;

public class MemberDelete {
    private String message;
    public MemberDelete(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
