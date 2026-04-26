package org.weather.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
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
                openweatherUrlLatAndLon(lat,lon),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<OpenWeatherDataDto>() {}
        ).getBody();
    }

    public List<OpenWeatherGeoDto> findSimilarCities(String city) throws HttpClientErrorException.NotFound, ResourceAccessException{
        return restTemplate.exchange(
                openweatherUrlSimilarCities(city), HttpMethod.GET,
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

    private URI openweatherUrlLatAndLon(BigDecimal lat, BigDecimal lon) {
        return UriComponentsBuilder
                .fromUriString(dataUrl)
                .queryParam("lat", lat.toString())
                .queryParam("lon", lon.toString())
                .queryParam("appid", API_KEY)
                .queryParam("units", "metric")
                .build()
                .toUri();
    }

    private URI openweatherUrlSimilarCities(String city) {
        return UriComponentsBuilder
                .fromUriString(geoUrl)
                .queryParam("q", city)
                .queryParam("limit", 5)
                .queryParam("appid", API_KEY)
                .build()
                .toUri();
    }
}
