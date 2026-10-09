
package com.example.wheather_api.controller;

import com.example.wheather_api.dto.WeatherResponse;
import com.example.wheather_api.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @Test
    void shouldReturnWeatherForCity() throws Exception {
        given(weatherService.getWeatherByCity("Agadir"))
                .willReturn(new WeatherResponse("Agadir", 19.6, 94, 3.4));

        mockMvc.perform(get("/api/weather").param("city", "Agadir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Agadir"))
                .andExpect(jsonPath("$.temperature").value(19.6))
                .andExpect(jsonPath("$.humidity").value(94))
                .andExpect(jsonPath("$.windSpeed").value(3.4));
    }
    @Test
    void shouldReturnBadRequestWhenCityIsMissing() throws Exception {
        mockMvc.perform(get("/api/weather"))
            .andExpect(status().isBadRequest());
          
}
      @Test
void shouldReturnNotFoundWhenCityDoesNotExist() throws Exception {
    given(weatherService.getWeatherByCity("XYZABC123"))
            .willThrow(new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "City not found: XYZABC123"
            ));

    mockMvc.perform(get("/api/weather").param("city", "XYZABC123"))
            .andExpect(status().isNotFound());
}
}