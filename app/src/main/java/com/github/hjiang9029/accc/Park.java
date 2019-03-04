package com.github.hjiang9029.accc;

public class Park {

    String name;
    String address;
    String category;
    String neighbourhood;
    double latitude;
    double longitude;


    public Park(String name, String address, String category, String neighbourhood, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.neighbourhood = neighbourhood;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }
}
