package com.login.loginsystem.service;

import com.login.loginsystem.dto.GeoLocationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${openweathermap.api.key}")
    private String weatherApiKey;
    private static final String QUERY_PARAM_APPID = "&appid=" ;
    private static final String QUERY_PARAM_UNITS = "&units=imperial" ;
    private static final String OPENWEATHERMAP_BASEURL = "http://api.openweathermap.org/data/2.5/weather?";

    @Value("${ipinfo.api.key}")
    private String ipinfoApiKey;
    private static final String QUERY_PARAM_TOKEN = "&token=" ;
    private static final String IPINFO_BASEURL = "http://ipinfo.io/";
    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getWeatherForLocation(String ipAddress) {
        GeoLocationDto location = getGeoLocation(ipAddress);
        String[] latLongValues = location.getLoc().split(",");
        String latParam = "&lat=" + latLongValues[0];
        String longParam = "&lon=" + latLongValues[1];
        String appIdKeyParam = QUERY_PARAM_APPID + weatherApiKey;
        String url = OPENWEATHERMAP_BASEURL + latParam + longParam + QUERY_PARAM_UNITS + appIdKeyParam;
        return restTemplate.getForObject(url, String.class);
    }

    private GeoLocationDto getGeoLocation(String ipAddress) {
        String url = IPINFO_BASEURL + ipAddress + "?" + QUERY_PARAM_TOKEN + ipinfoApiKey;
        return restTemplate.getForObject(url, GeoLocationDto.class);
    }
}