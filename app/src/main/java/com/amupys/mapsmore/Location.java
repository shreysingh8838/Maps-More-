package com.amupys.mapsmore;

public class Location {
    private String id, name, description;
    private double Latitude, Longitude;
    private int numAcc, speedLim;
    private Long timeOfAcc;

    public Long getTimeOfAcc() {
        return timeOfAcc;
    }

    public void setTimeOfAcc(Long timeOfAcc) {
        this.timeOfAcc = timeOfAcc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location(String id, String name, String description, double latitude, double longitude, int numAcc, int speedLim) {
        this.id = id;
        this.name = name;
        this.description = description;
        Latitude = latitude;
        Longitude = longitude;
        this.numAcc = numAcc;
        this.speedLim = speedLim;
    }

    public int getSpeedLim() {
        return speedLim;
    }

    public void setSpeedLim(int speedLim) {
        this.speedLim = speedLim;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public int getNumAcc() {
        return numAcc;
    }

    public void setNumAcc(int numAcc) {
        this.numAcc = numAcc;
    }
}
