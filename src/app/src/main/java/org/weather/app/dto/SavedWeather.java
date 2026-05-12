package org.weather.app.dto;

public record SavedWeather(Integer locationId, OpenWeatherData response) {
}
