package br.com.infotransctd;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RouteDate {

    private String initDate;
    private String finishDate;
    private ArrayList<LocationData> listOfLocation;
    private LocationData tempLocation;
    private Double averageOfSpeed;
    private String meansOfTransport;
    private String cityName;

    public RouteDate(LocationData tempLocation, String meansOfTransport, String cityName){
        this.tempLocation = tempLocation;
        this.meansOfTransport = meansOfTransport;
        this.cityName = cityName;
    }

    public RouteDate(String initDate, String finishDate, ArrayList<LocationData>listOfLocation, Double averageOfSpeed, String meansOfTransport, String cityName) {
        this.initDate = initDate;
        this.finishDate = finishDate;
        this.listOfLocation = listOfLocation;
        this.averageOfSpeed = averageOfSpeed;
        this.meansOfTransport = meansOfTransport;
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getInitDate() {
        return initDate;
    }

    public void setInitDate(String initDate) {
        this.initDate = initDate;
    }

    public String getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(String finishDate) {
        this.finishDate = finishDate;
    }

    public ArrayList<LocationData> getListOfLocation() {
        return listOfLocation;
    }

    public void setListOfLocation(ArrayList<LocationData> listOfLocation) {
        this.listOfLocation = listOfLocation;
    }

    public Double getAverageOfSpeed() {
        return averageOfSpeed;
    }

    public void setAverageOfSpeed(Double averageOfSpeed) {
        this.averageOfSpeed = averageOfSpeed;
    }

    public String getMeansOfTransport() {
        return meansOfTransport;
    }

    public void setMeansOfTransport(String meansOfTransport) {
        this.meansOfTransport = meansOfTransport;
    }

    public LocationData getTempLocation() {
        return tempLocation;
    }

    public void setTempListOfLocation(LocationData tempLocation) {
        this.tempLocation = tempLocation;
    }

}
