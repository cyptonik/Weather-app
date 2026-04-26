package org.weather.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.weather.app.dto.OpenWeatherDataDto;
import org.weather.app.dto.OpenWeatherGeoDto;
import org.weather.app.model.Location;
import org.weather.app.model.User;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherService {
    private final RestTemplate restTemplate;

    @Value("${openweather.data.url}")
    private String dataUrl;

    @Value("${openweather.geo.url}")
    private String geoUrl;

    @Value("${openweather.api.key}")
    private String API_KEY;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OpenWeatherDataDto findWeatherByLatAndLon(BigDecimal lat, BigDecimal lon) throws HttpClientErrorException.NotFound, ResourceAccessException {
        return restTemplate.exchange(
                URI.create(openweatherUrlLatAndLon(lat, lon)),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<OpenWeatherDataDto>() {}
        ).getBody();
    }

    public List<OpenWeatherGeoDto> findSimilarCities(String city) throws HttpClientErrorException.NotFound, ResourceAccessException{
        return restTemplate.exchange(
                URI.create(openweatherUrlSimilarCities(city)), HttpMethod.GET,
                null, new ParameterizedTypeReference<List<OpenWeatherGeoDto>>() {
                }).getBody();
    }

    public Optional<Location> buildLocation(User user, String latitude, String longitude) {
        if (latitude == null || longitude == null) {
            return Optional.empty();
        }

        Location location = new Location();
        location.setUser(user);
        location.setLatitude(new BigDecimal(latitude));
        location.setLongitude(new BigDecimal(longitude));

        return Optional.of(location);
    }

    private String openweatherUrlLatAndLon(BigDecimal lat, BigDecimal lon) {
        return dataUrl + "lat=" + lat.toString() + "&lon=" + lon.toString() + "&units=metric" + "&APPID=" + API_KEY;
    }

    private String openweatherUrlSimilarCities(String city) {
        return geoUrl + "q=" + city + "&limit=5&units=metric" + "&APPID=" + API_KEY;
    }
}
