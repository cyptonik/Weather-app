package org.weather.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.weather.app.ErrorMessage;
import org.weather.app.dto.OpenWeatherGeoDto;
import org.weather.app.model.Location;
import org.weather.app.model.User;
import org.weather.app.model.UserSession;
import org.weather.app.repository.LocationRepository;
import org.weather.app.service.WeatherService;

import java.util.List;
import java.util.Optional;

@Controller
public class WeatherController {
    @Value("${city.max.length}")
    private int maxCityLength;

    private final LocationRepository locationRepository;
    private final WeatherService weatherService;

    public WeatherController(LocationRepository locationRepository, WeatherService weatherService) {
        this.locationRepository = locationRepository;
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public String get(HttpServletRequest request) {
        UserSession userSession = (UserSession) request.getAttribute("currentSession");

        User currentUser = userSession.getUser();
        List<Location> locations = locationRepository.findAllByUserId(currentUser.getId());

        request.setAttribute("savedWeathers", weatherService.mapToSavedWeatherDto(locations));
        request.setAttribute("login", currentUser.getLogin());
        return "weather";
    }

    @PostMapping("/weather")
    public String search(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String city = request.getParameter("city");
        if (city == null || city.isBlank() || city.length() > maxCityLength) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_CITY);
            return "redirect:/weather";
        }

        List<OpenWeatherGeoDto> citiesDto;
        try {
            citiesDto = weatherService.findSimilarCities(city);
        } catch (HttpClientErrorException.NotFound e) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.CITY_NOT_FOUND);
            return "redirect:/weather";
        } catch (ResourceAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.TIMEOUT);
            return "redirect:/weather";
        }

        if (citiesDto.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.CITY_NOT_FOUND);
            return "redirect:/weather";
        }

        redirectAttributes.addFlashAttribute("foundWeathers", weatherService.mapToOpenWeatherDataDto(citiesDto));
        return "redirect:/weather";
    }

    @PostMapping("/saveWeather")
    public String save(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        UserSession userSession = (UserSession) request.getAttribute("currentSession");

        String latitude = request.getParameter("lat");
        String longitude = request.getParameter("lon");
        String city = request.getParameter("city");
        Optional<Location> saveLocation = weatherService.buildLocation(userSession.getUser(), city, latitude, longitude);
        if (saveLocation.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_CITY);
            return "redirect:/weather";
        }

        if (locationRepository.findAllByUserId(userSession.getUser().getId()).stream().anyMatch(loc ->
                loc.getLatitude().equals(saveLocation.get().getLatitude()) &&
                loc.getLongitude().equals(saveLocation.get().getLongitude()))) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.ALREADY_SAVED);
            return "redirect:/weather";
        }

        locationRepository.save(saveLocation.get());
        return "redirect:/weather";
    }

    @PostMapping("/deleteWeather")
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
