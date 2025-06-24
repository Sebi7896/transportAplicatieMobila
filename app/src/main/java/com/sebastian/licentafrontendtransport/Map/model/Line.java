package com.sebastian.licentafrontendtransport.Map.model;

import java.io.Serializable;
import java.util.List;

public class Line implements Serializable {
    public String name;
    public String short_name;
    public String color;
    public String text_color;
    public List<Agency> agencies;
    public Vehicle vehicle;
}
