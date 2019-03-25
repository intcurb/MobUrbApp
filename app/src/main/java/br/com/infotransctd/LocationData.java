package br.com.infotransctd;

import java.util.Date;

public class LocationData {

    private double latitude;
    private double longitude;
    private String dateNow;

    public LocationData(){

    }

    public LocationData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationData(double latitude, double longitude, String dateNow) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateNow = dateNow;
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

    public String getDateNow() {
        return dateNow;
    }

    public void setDateNow(String dateNow) {
        this.dateNow = dateNow;
    }
}
