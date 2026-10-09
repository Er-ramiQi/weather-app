package com.example.wheather_api.dto;

public class WeatherResponse {
  private String city;
    private double temperature;
    private int humidity;
    private double windSpeed;

    public WeatherResponse(
            String city,
            double temperature,
            int humidity,
            double windSpeed) {

        this.city = city;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    public String getCity() {
        return city;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}
