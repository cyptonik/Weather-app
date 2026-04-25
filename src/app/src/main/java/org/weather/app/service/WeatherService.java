package org.weather.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.weather.app.dto.OpenWeatherDto;
import org.weather.app.model.Location;
import org.weather.app.model.User;
import org.weather.app.repository.LocationRepository;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Optional;

@Service
public class WeatherService {
    private final LocationRepository locationRepository;
    private final RestTemplate restTemplate;

    @Value("${openweather.url}")
    private String url;

    @Value("${openweather.api.key}")
    private String API_KEY;

    public WeatherService(LocationRepository locationRepository, RestTemplate restTemplate) {
        this.locationRepository = locationRepository;
        this.restTemplate = restTemplate;
    }

    public OpenWeatherDto findWeatherByLatAndLon(BigDecimal lat, BigDecimal lon) throws HttpClientErrorException.NotFound, ResourceAccessException {
        return restTemplate.exchange(
                URI.create(openweatherUrlLatAndLon(lat, lon)),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<OpenWeatherDto>() {}
        ).getBody();
    }

    public OpenWeatherDto findWeatherByCity(String city) throws HttpClientErrorException.NotFound, ResourceAccessException{
        ResponseEntity<OpenWeatherDto> openweatherResponse;
        openweatherResponse = restTemplate.exchange(
                URI.create(openweatherUrlCity(city)), HttpMethod.GET,
                null, new ParameterizedTypeReference<>() {
                });

        return openweatherResponse.getBody();
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

    private String openweatherUrlCity(String city) {
        return url + "q=" + city + "&units=metric" + "&APPID=" + API_KEY;
    }

    private String openweatherUrlLatAndLon(BigDecimal lat, BigDecimal lon) {
        return url + "lat=" + lat.toString() + "&lon=" + lon.toString() + "&units=metric" + "&APPID=" + API_KEY;
    }
}
