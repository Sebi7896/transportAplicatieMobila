package com.sebastian.licentafrontendtransport.Map.model;

import java.io.Serializable;

public class LocationOption implements Serializable {
    private String name;
    private double latitude;
    private double longitude;

    public LocationOption(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setName(String home) {
        this.name = home;

    }
}