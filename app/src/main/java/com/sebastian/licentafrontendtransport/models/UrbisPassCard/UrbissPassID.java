package com.sebastian.licentafrontendtransport.models.UrbisPassCard;

public class UrbissPassID {
    private String cnpPayload;

    public UrbissPassID(String id) {
        this.cnpPayload = id;
    }

    public String getId() {
        return cnpPayload;
    }

    public void setId(String id) {
        this.cnpPayload = id;
    }
}
