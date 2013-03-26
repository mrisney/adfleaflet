package org.risney.adf.model;

public class Weather {
    private String stationName;
    private String ICAOCode;
    private String weatherCondition;
    private String temperature;
    private String humidity;
    private String clouds;
    private String windSpeed;
    private String elevation;
    private String weatherIcon;

    public Weather() {
        super();
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    public String getElevation() {
        return elevation;
    }

    public void setClouds(String clouds) {
        this.clouds = clouds;
    }

    public String getClouds() {
        return clouds;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setICAOCode(String ICAOCode) {
        this.ICAOCode = ICAOCode;
    }

    public String getICAOCode() {
        return ICAOCode;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }
}
