package com.login.loginsystem.service;

import com.login.loginsystem.dto.GeoLocationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;
    private final String weatherApiKey = "null";
    private final String ipinfoApiKey = "null";
    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWeatherForLocation() {
        // Arrange
        String ipAddress = "192.168.1.1";
        GeoLocationDto location = new GeoLocationDto();
        location.setLoc("37.7749,-122.4194"); // Mocking latitude and longitude

        String expectedWeatherResponse = "{\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01d\"}]}"; // Sample JSON response

        String ipInfoUrl = "http://ipinfo.io/192.168.1.1?&token=" + ipinfoApiKey;
        when(restTemplate.getForObject(ipInfoUrl, GeoLocationDto.class)).thenReturn(location);

        String weatherApiUrl = "http://api.openweathermap.org/data/2.5/weather?&lat=37.7749&lon=-122.4194&units=imperial&appid=" + weatherApiKey;
        when(restTemplate.getForObject(weatherApiUrl, String.class)).thenReturn(expectedWeatherResponse);

        // Act
        String weatherResponse = weatherService.getWeatherForLocation(ipAddress);

        // Assert
        assertThat(weatherResponse).isNotNull();
        assertThat(weatherResponse).isEqualTo(expectedWeatherResponse);
    }
}
