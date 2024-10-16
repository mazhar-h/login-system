package com.login.loginsystem.dto;

import lombok.Data;

@Data
public class GeoLocationDto {
    private String ip;
    private String city;
    private String region;
    private String country;
    private String loc;
}