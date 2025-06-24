package com.sebastian.licentafrontendtransport.models.UrbisPassCard;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Person implements Serializable {
    @SerializedName("cnp")
    private String cnp;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("isStudent")
    private boolean isStudent;

    // If there are other fields, add here with matching @SerializedName.

    // Constructors
    public Person() {}

    public Person(String cnp, String fullName, boolean isStudent) {
        this.cnp = cnp;
        this.fullName = fullName;
        this.isStudent = isStudent;
    }

    // Getters & setters
    public String getCnp() { return cnp; }
    public void setCnp(String cnp) { this.cnp = cnp; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public boolean isStudent() { return isStudent; }
    public void setStudent(boolean student) { isStudent = student; }
}