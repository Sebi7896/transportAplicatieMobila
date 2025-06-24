package com.sebastian.licentafrontendtransport.Map.model;

import com.google.android.gms.maps.model.Polyline;

import java.io.Serializable;
import java.util.List;

public class Leg implements Serializable {
    public TextValue arrival_time;
    public TextValue departure_time;
    public TextValue distance;
    public TextValue duration;
    public List<Step> steps;
    public String end_address;
    public Location end_location;
    public String start_address;
    public Location start_location;
    public Polyline overview_polyline;
}
