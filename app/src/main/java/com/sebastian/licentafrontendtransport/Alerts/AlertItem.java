package com.sebastian.licentafrontendtransport.Alerts;


import com.google.firebase.Timestamp;

public class AlertItem {

    public String title;
    public String message;
    public Timestamp timestamp;
    public AlertItem() { }
    public AlertItem(String title, String message, Timestamp timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}