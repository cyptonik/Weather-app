package org.weather.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.weather.app.dto.OpenWeatherData;
import org.weather.app.dto.OpenWeatherGeo;
import org.weather.app.dto.SavedWeather;
import org.weather.app.model.Location;
import org.weather.app.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class WeatherService {
    private final RestTemplate restTemplate;

    @Value("${openweather.data.url}")
    private String dataUrl;

    @Value("${openweather.geo.url}")
    private String geoUrl;

    @Value("${openweather.api.key}")
    private String apiKey;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OpenWeatherData findWeatherByLatAndLon(BigDecimal lat, BigDecimal lon) throws HttpClientErrorException.NotFound, ResourceAccessException {
        return restTemplate.exchange(
                openweatherUrlLatAndLon(lat,lon),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<OpenWeatherData>() {}
        ).getBody();
    }

    public List<OpenWeatherGeo> findSimilarCities(String city) throws HttpClientErrorException.NotFound, ResourceAccessException{
        return restTemplate.exchange(
                openweatherUrlSimilarCities(city), HttpMethod.GET,
                null, new ParameterizedTypeReference<List<OpenWeatherGeo>>() {
                }).getBody();
    }

    public Optional<Location> buildLocation(User user, String name, String latitude, String longitude) {
        Location location = new Location();
        location.setUser(user);
        location.setLatitude(new BigDecimal(latitude));
        location.setLongitude(new BigDecimal(longitude));
        location.setName(name);

        return Optional.of(location);
    }

    private URI openweatherUrlLatAndLon(BigDecimal lat, BigDecimal lon) {
        return UriComponentsBuilder
                .fromUriString(dataUrl)
                .queryParam("lat", lat.toString())
                .queryParam("lon", lon.toString())
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUri();
    }

    private URI openweatherUrlSimilarCities(String city) {
        return UriComponentsBuilder
                .fromUriString(geoUrl)
                .queryParam("q", city)
                .queryParam("limit", 5)
                .queryParam("appid", apiKey)
                .build()
                .toUri();
    }

    public List<OpenWeatherData> mapToOpenWeatherDataDto(List<OpenWeatherGeo> citiesDto) {
        return citiesDto.stream()
                .map(dto -> {
                    OpenWeatherData weather = findWeatherByLatAndLon(dto.lat, dto.lon);
                    weather.name = dto.name;
                    return weather;
                })
                .filter(distinctByKey(dto ->
                        dto.coord.lat.setScale(1, RoundingMode.HALF_UP) + "," +
                                dto.coord.lon.setScale(1, RoundingMode.HALF_UP)
                ))
                .collect(Collectors.toList());
    }

    public List<SavedWeather> mapToSavedWeatherDto(List<Location> locations) {
        return locations.stream()
                .map(loc -> {
                    SavedWeather dto = new SavedWeather(
                            loc.getId(),
                            findWeatherByLatAndLon(loc.getLatitude(), loc.getLongitude()));
                    dto.response().name = loc.getName();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<T, Object> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
