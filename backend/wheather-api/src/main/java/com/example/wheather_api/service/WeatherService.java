
package com.example.wheather_api.service;

import com.example.wheather_api.dto.WeatherResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    private final RestClient geocodingClient;
    private final RestClient weatherClient;

    

    public WeatherService() {
        this.geocodingClient = RestClient.builder()
                .baseUrl("https://geocoding-api.open-meteo.com")
                .build();

        this.weatherClient = RestClient.builder()
                .baseUrl("https://api.open-meteo.com")
                .build();
    }
    
    WeatherService(RestClient.Builder geocodingBuilder,RestClient.Builder weatherBuilder) {
    this.geocodingClient = geocodingBuilder.build();
    this.weatherClient = weatherBuilder.build();
    }

    public WeatherResponse getWeatherByCity(String city) {

        // 1. Search for the city
        Map<?, ?> geocodingResponse = geocodingClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(Map.class);

        if (geocodingResponse == null
                || !(geocodingResponse.get("results") instanceof List<?> results)
                || results.isEmpty()
                || !(results.get(0) instanceof Map<?, ?> location)) {
            throw new ResponseStatusException(
              HttpStatus.NOT_FOUND,
              "City not found: " + city
            );
        }

        double latitude =
                ((Number) location.get("latitude")).doubleValue();

        double longitude =
                ((Number) location.get("longitude")).doubleValue();

        String cityName = location.get("name").toString();

        // 2. Get current weather
        Map<?, ?> weatherResponse = weatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam(
                                "current",
                                "temperature_2m,relative_humidity_2m,wind_speed_10m"
                        )
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(Map.class);

        if (weatherResponse == null
                || !(weatherResponse.get("current") instanceof Map<?, ?> current)) {
            throw new IllegalStateException("Weather data is unavailable");
        }

        double temperature =
                ((Number) current.get("temperature_2m")).doubleValue();

        int humidity =
                ((Number) current.get("relative_humidity_2m")).intValue();

        double windSpeed =
                ((Number) current.get("wind_speed_10m")).doubleValue();

        // 3. Return our clean response
        return new WeatherResponse(
                cityName,
                temperature,
                humidity,
                windSpeed
        );
    }
}