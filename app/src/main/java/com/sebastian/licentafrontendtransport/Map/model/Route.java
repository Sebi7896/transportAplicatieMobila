package com.sebastian.licentafrontendtransport.Map.model;


import java.io.Serializable;
import java.util.List;

public class Route implements Serializable {
    public List<Leg> legs;
    public Polyline overview_polyline;
}
