package com.example.wheather_api.controller;

import com.example.wheather_api.dto.WeatherResponse;
import com.example.wheather_api.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/api/weather")
    public WeatherResponse getWeather(
            @RequestParam("city") String city) {

        return weatherService.getWeatherByCity(city);
    }
}