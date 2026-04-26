package org.weather.app.dto;

public class SavedWeatherDto {
    public Integer locationId;
    public OpenWeatherDataDto response;

    public SavedWeatherDto(Integer locationId, OpenWeatherDataDto response) {
        this.locationId = locationId;
        this.response = response;
    }
}
