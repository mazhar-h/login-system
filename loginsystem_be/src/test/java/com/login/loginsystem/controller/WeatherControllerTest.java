package com.login.loginsystem.controller;

import com.login.loginsystem.service.WeatherService;
import com.login.loginsystem.util.IpAddressUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private WeatherController weatherController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWeather_Success() {
        String ipAddress = "192.168.1.1";
        String expectedWeatherData = "Sunny, 25°C";

        // Mock the utility method to return a specific IP address
        when(IpAddressUtil.getClientIp(request)).thenReturn(ipAddress);
        // Mock the weather service to return weather data based on the IP address
        when(weatherService.getWeatherForLocation(ipAddress)).thenReturn(expectedWeatherData);

        ResponseEntity<String> response = weatherController.getWeather(request);

        // Assert the response
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedWeatherData, response.getBody());
    }

    @Test
    void testGetWeather_Failure() {
        String ipAddress = "192.168.1.1";
        String errorMessage = "Weather data not found";

        // Mock the utility method to return a specific IP address
        when(IpAddressUtil.getClientIp(request)).thenReturn(ipAddress);
        // Mock the weather service to return an error message
        when(weatherService.getWeatherForLocation(ipAddress)).thenReturn(null);

        ResponseEntity<String> response = weatherController.getWeather(request);

        // Assert the response
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(null, response.getBody());
    }
}