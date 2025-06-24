package com.sebastian.licentafrontendtransport.Lines;

import java.util.List;

public class MetroLine {
    private String name;
    private String color;
    private String description;
    private String funFact;
    private List<String> stations;

    public String getName() { return name; }
    public String getColor() { return color; }
    public String getDescription() { return description; }
    public String getFunFact() { return funFact; }
    public List<String> getStations() { return stations; }
}