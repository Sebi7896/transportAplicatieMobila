package com.sebastian.licentafrontendtransport.Map.model;

import java.io.Serializable;

public class TransitDetails implements Serializable {
    public Stop arrival_stop;
    public Stop departure_stop;
    public TextValue arrival_time;
    public TextValue departure_time;
    public String headsign;
    public Line line;
    public int num_stops;
}