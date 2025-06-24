package com.sebastian.licentafrontendtransport.Map;

import com.google.android.gms.maps.model.LatLng;

public class ShortcutLocation {
    public final String name;
    public final LatLng latLng;

    public ShortcutLocation(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}