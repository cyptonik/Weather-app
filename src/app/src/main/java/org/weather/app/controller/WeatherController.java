package org.weather.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.weather.app.ErrorMessage;
import org.weather.app.dto.OpenWeatherDataDto;
import org.weather.app.dto.OpenWeatherGeoDto;
import org.weather.app.dto.SavedWeatherDto;
import org.weather.app.model.Location;
import org.weather.app.model.User;
import org.weather.app.model.UserSession;
import org.weather.app.repository.LocationRepository;
import org.weather.app.service.WeatherService;

import java.util.List;
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
    public ResponseEntity<List<SavedWeatherDto>> get(@RequestParam("id") String id) {
        List<Location> locations = locationRepository.findAllByUserId(Integer.valueOf(id));
        return new ResponseEntity<>(weatherService.mapToSavedWeatherDto(locations), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<OpenWeatherDataDto>> search(@RequestParam("city") String city) {
        if (city.isBlank() || city.length() > maxCityLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessage.CITY_NOT_FOUND);
        }

        List<OpenWeatherGeoDto> citiesDto = weatherService.findSimilarCities(city);
        if (citiesDto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessage.CITY_NOT_FOUND);
        }

        return new ResponseEntity<>(weatherService.mapToOpenWeatherDataDto(citiesDto), HttpStatus.OK);
    }

    @PostMapping
    public Location save(@RequestParam("lat") String latitude,
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

        return locationRepository.save(saveLocation.get());
    }

    @DeleteMapping
    public String delete(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        UserSession userSession = (UserSession) request.getAttribute("currentSession");

        String locationId = request.getParameter("locationId");
        if (locationId == null || locationId.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.CITY_NOT_FOUND);
            return "redirect:/weather";
        }

        Location deleteLocation = locationRepository.findById(Integer.valueOf(locationId));
        if (deleteLocation == null) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_CITY);
            return "redirect:/weather";
        }

        if (!deleteLocation.getUser().getId().equals(userSession.getUser().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_PARAMS);
            return "redirect:/weather";
        }

        locationRepository.delete(deleteLocation);
        return "redirect:/weather";
    }
}
