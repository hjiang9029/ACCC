package com.github.hjiang9029.accc;

public class ParkStructure {

    String type;
    String parkName;
    double latitude;
    double longitude;

    public ParkStructure(String type, String parkName, double latitude, double longitude) {
        this.type = type;
        this.parkName = parkName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParkName() {
        return parkName;
    }

    public void setParkName(String parkName) {
        this.parkName = parkName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
