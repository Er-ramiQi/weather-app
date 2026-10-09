
package com.example.wheather_api.service;

import com.example.wheather_api.dto.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WeatherServiceTest {

    private MockRestServiceServer geocodingServer;
    private MockRestServiceServer weatherServer;
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        RestClient.Builder geocodingBuilder = RestClient.builder()
                .baseUrl("https://geocoding-api.open-meteo.com");

        RestClient.Builder weatherBuilder = RestClient.builder()
                .baseUrl("https://api.open-meteo.com");

        geocodingServer = MockRestServiceServer.bindTo(geocodingBuilder).build();
        weatherServer = MockRestServiceServer.bindTo(weatherBuilder).build();

        // Le service utilise ses propres clients dans son constructeur.
        // Nous utiliserons une version injectable dans le service pour les tests.
        weatherService = new WeatherService(geocodingBuilder, weatherBuilder);
    }

    @Test
    void shouldReturnWeatherForValidCity() {
        geocodingServer.expect(requestTo(
                        "https://geocoding-api.open-meteo.com/v1/search?name=Agadir&count=1&language=en&format=json"))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "name": "Agadir",
                              "latitude": 30.42,
                              "longitude": -9.58
                            }
                          ]
                        }
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        weatherServer.expect(requestTo(
                        "https://api.open-meteo.com/v1/forecast?latitude=30.42&longitude=-9.58&current=temperature_2m,relative_humidity_2m,wind_speed_10m&timezone=auto"))
                .andRespond(withSuccess("""
                        {
                          "current": {
                            "temperature_2m": 19.6,
                            "relative_humidity_2m": 94,
                            "wind_speed_10m": 3.4
                          }
                        }
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        WeatherResponse response = weatherService.getWeatherByCity("Agadir");

        assertEquals("Agadir", response.getCity());
        assertEquals(19.6, response.getTemperature(), 0.01);
        assertEquals(94, response.getHumidity());
        assertEquals(3.4, response.getWindSpeed(), 0.01);

        geocodingServer.verify();
        weatherServer.verify();
    }

    @Test
    void shouldThrow404WhenCityDoesNotExist() {
        geocodingServer.expect(requestTo(
                        "https://geocoding-api.open-meteo.com/v1/search?name=XYZABC123&count=1&language=en&format=json"))
                .andRespond(withSuccess("""
                        {"results": []}
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> weatherService.getWeatherByCity("XYZABC123")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        geocodingServer.verify();
    }

    @Test
    void shouldThrowWhenWeatherDataIsUnavailable() {
        geocodingServer.expect(requestTo(
                        "https://geocoding-api.open-meteo.com/v1/search?name=Agadir&count=1&language=en&format=json"))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "name": "Agadir",
                              "latitude": 30.42,
                              "longitude": -9.58
                            }
                          ]
                        }
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        weatherServer.expect(requestTo(
                        "https://api.open-meteo.com/v1/forecast?latitude=30.42&longitude=-9.58&current=temperature_2m,relative_humidity_2m,wind_speed_10m&timezone=auto"))
                .andRespond(withSuccess("""
                        {}
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> weatherService.getWeatherByCity("Agadir")
        );

        assertEquals("Weather data is unavailable", exception.getMessage());

        geocodingServer.verify();
        weatherServer.verify();
    }
}