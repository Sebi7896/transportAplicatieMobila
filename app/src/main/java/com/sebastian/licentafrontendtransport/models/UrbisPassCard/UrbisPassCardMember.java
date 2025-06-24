package com.sebastian.licentafrontendtransport.models.UrbisPassCard;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UrbisPassCardMember implements Serializable {
    @SerializedName("cnp")
    private String cnp;

    @SerializedName("status")
    private String status;

    @SerializedName("expirationDate")
    private String expirationDate;

    @SerializedName("lastValidatedAt")
    private String lastValidatedAt;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("person")
    private com.sebastian.licentafrontendtransport.models.UrbisPassCard.Person person;

    // Constructors
    public UrbisPassCardMember() {}

    public UrbisPassCardMember(String cnp, String status, String expirationDate, String lastValidatedAt, String createdBy, com.sebastian.licentafrontendtransport.models.UrbisPassCard.Person person) {
        this.cnp = cnp;
        this.status = status;
        this.expirationDate = expirationDate;
        this.lastValidatedAt = lastValidatedAt;
        this.createdBy = createdBy;
        this.person = person;
    }

    // Getters & setters
    public String getCnp() { return cnp; }
    public void setCnp(String cnp) { this.cnp = cnp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getLastValidatedAt() { return lastValidatedAt; }
    public void setLastValidatedAt(String lastValidatedAt) { this.lastValidatedAt = lastValidatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
