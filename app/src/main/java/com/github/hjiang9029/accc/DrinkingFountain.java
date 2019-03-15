package com.github.hjiang9029.accc;

public class DrinkingFountain {


    String parkName;
    double latitude;
    double longitude;

    public DrinkingFountain(String parkName, double latitude, double longitude) {
        this.parkName = parkName;
        this.latitude = latitude;
        this.longitude = longitude;
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
