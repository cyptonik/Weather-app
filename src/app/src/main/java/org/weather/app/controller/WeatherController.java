package org.weather.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.weather.app.ErrorMessage;
import org.weather.app.dto.OpenWeatherGeo;
import org.weather.app.dto.SavedWeather;
import org.weather.app.model.Location;
import org.weather.app.model.UserSession;
import org.weather.app.repository.LocationRepository;
import org.weather.app.service.WeatherService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    @Value("${city.max.length}")
    private int maxCityLength;

    private final LocationRepository locationRepository;
    private final WeatherService weatherService;

    public WeatherController(LocationRepository locationRepository, WeatherService weatherService) {
        this.locationRepository = locationRepository;
        this.weatherService = weatherService;
    }

    @GetMapping
    public ResponseEntity<List<SavedWeather>> get(HttpServletRequest request) {
        UserSession session = (UserSession) request.getAttribute("currentSession");
        List<Location> locations = locationRepository.findAllByUserId(session.getUser().getId());
        return ResponseEntity.ok(weatherService.mapToSavedWeatherDto(locations));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam("city") String city) {
        if (city.isBlank() || city.length() > maxCityLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessage.CITY_NOT_FOUND);
        }

        List<OpenWeatherGeo> citiesDto = weatherService.findSimilarCities(city);
        if (citiesDto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessage.CITY_NOT_FOUND);
        }

        return new ResponseEntity<>(weatherService.mapToOpenWeatherDataDto(citiesDto), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestParam("lat") String latitude,
                                  @RequestParam("lon") String longitude,
                                  @RequestParam("city") String city,
                                  HttpServletRequest request) {
        UserSession userSession = (UserSession) request.getAttribute("currentSession");

        Optional<Location> saveLocation = weatherService.buildLocation(userSession.getUser(), city, latitude, longitude);
        if (saveLocation.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_CITY);
        }

        if (locationRepository.findAllByUserId(userSession.getUser().getId()).stream()
                .anyMatch(loc ->
                        loc.getLatitude().equals(saveLocation.get().getLatitude()) &&
                                loc.getLongitude().equals(saveLocation.get().getLongitude()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessage.ALREADY_SAVED);
        }

        locationRepository.save(saveLocation.get());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<?> delete(@PathVariable Integer locationId, HttpServletRequest request) {
        UserSession userSession = (UserSession) request.getAttribute("currentSession");

        Location location = locationRepository.findById(locationId);
        if (location == null) {
            return ResponseEntity.status(404).body(Map.of("error", ErrorMessage.INVALID_CITY));
        }
        if (!location.getUser().getId().equals(userSession.getUser().getId())) {
            return ResponseEntity.status(403).body(Map.of("error", ErrorMessage.INVALID_PARAMS));
        }

        locationRepository.delete(location);
        return ResponseEntity.ok().build();
    }
}
