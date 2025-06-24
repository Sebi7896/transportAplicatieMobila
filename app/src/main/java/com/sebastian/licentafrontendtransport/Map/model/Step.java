package com.sebastian.licentafrontendtransport.Map.model;

import java.io.Serializable;
import java.util.List;

public class Step implements Serializable {
    public TextValue distance;
    public TextValue duration;
    public Location start_location;
    public Location end_location;
    public String html_instructions;
    public Polyline polyline;
    public List<Step> steps;
    public String travel_mode;
    public String maneuver;
    public TransitDetails transit_details;
}
