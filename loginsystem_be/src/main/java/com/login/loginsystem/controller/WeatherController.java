package com.login.loginsystem.controller;

import com.login.loginsystem.service.WeatherService;
import com.login.loginsystem.util.IpAddressUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/api/v1/weather")
    public ResponseEntity<String> getWeather(HttpServletRequest request) {
        String ipAddress = IpAddressUtil.getClientIp(request);
        String weatherData = weatherService.getWeatherForLocation(ipAddress);
        return ResponseEntity.ok(weatherData);
    }
}