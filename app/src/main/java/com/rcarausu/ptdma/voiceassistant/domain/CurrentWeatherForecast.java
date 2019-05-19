package com.rcarausu.ptdma.voiceassistant.domain;

public class CurrentWeatherForecast {
    private String city;
    private String summary;
    private Double temperature;
    private String icon;
    private Double relativeHumidity;

    public CurrentWeatherForecast(String city, String summary, Double temperature, String icon, Double relativeHumidity) {
        this.city = city;
        this.summary = summary;
        this.temperature = temperature;
        this.icon = icon;
        this.relativeHumidity = relativeHumidity;
    }

    public String getSummary() {
        return summary;
    }

    public Double getTemperature() {
        return temperature;
    }

    public String getIcon() {
        return icon;
    }

    public Double getRelativeHumidity() {
        return relativeHumidity;
    }

    public String getCity() {
        return city;
    }
}
