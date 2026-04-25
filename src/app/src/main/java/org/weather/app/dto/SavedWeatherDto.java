package org.weather.app.dto;

public class SavedWeatherDto {
    public Integer locationId;
    public OpenWeatherDto response;

    public SavedWeatherDto(Integer locationId, OpenWeatherDto response) {
        this.locationId = locationId;
        this.response = response;
    }
}
