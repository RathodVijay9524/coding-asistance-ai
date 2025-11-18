package com.vijay.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class AIAgentToolServiceTest {

    private AIAgentToolService service;

    @BeforeEach
    void setUp() {
        // AIAgentToolService uses internal ObjectMapper and HttpClient instances
        service = new AIAgentToolService();
    }

    private void setField(String name, Object value) throws Exception {
        Field field = AIAgentToolService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }

    @Test
    @DisplayName("add should return sum of two numbers")
    void add_returnsSum() {
        int result = service.add(2, 3);
        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("multiply should return product of two numbers")
    void multiply_returnsProduct() {
        int result = service.multiply(4, 5);
        assertThat(result).isEqualTo(20);
    }

    @Test
    @DisplayName("getCurrentWeather should return configuration error when API key is missing")
    void getCurrentWeather_missingApiKey_returnsError() throws Exception {
        setField("openWeatherApiKey", "");
        AIAgentToolService.WeatherRequest request = new AIAgentToolService.WeatherRequest("London", "metric");

        String result = service.getCurrentWeather(request);

        assertThat(result).contains("Error: Weather is not configured");
    }

    @Test
    @DisplayName("getWeatherForecast should return configuration error when API key is missing")
    void getWeatherForecast_missingApiKey_returnsError() throws Exception {
        setField("openWeatherApiKey", "");
        AIAgentToolService.WeatherRequest request = new AIAgentToolService.WeatherRequest("London", "metric");

        String result = service.getWeatherForecast(request);

        assertThat(result).contains("Error: Weather is not configured");
    }

    @Test
    @DisplayName("googleSearch should return configuration error when SerpAPI key is missing")
    void googleSearch_missingApiKey_returnsError() throws Exception {
        setField("serpapiApiKey", "");
        AIAgentToolService.GoogleSearchRequest request = new AIAgentToolService.GoogleSearchRequest("spring boot", 3);

        String result = service.googleSearch(request);

        assertThat(result).contains("Error: Search is not configured");
    }
}
