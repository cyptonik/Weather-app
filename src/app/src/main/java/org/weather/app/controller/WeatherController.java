package org.weather.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.weather.app.ErrorMessage;
import org.weather.app.dto.OpenWeatherDto;
import org.weather.app.dto.SavedWeatherDto;
import org.weather.app.model.Location;
import org.weather.app.model.User;
import org.weather.app.model.UserSession;
import org.weather.app.repository.LocationRepository;
import org.weather.app.service.SessionService;
import org.weather.app.service.WeatherService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class WeatherController {
    private final SessionService sessionService;
    private final LocationRepository locationRepository;
    private final WeatherService weatherService;

    public WeatherController(SessionService sessionService, LocationRepository locationRepository, WeatherService weatherService) {
        this.sessionService = sessionService;
        this.locationRepository = locationRepository;
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public String get(HttpServletRequest request) {
        UserSession userSession = sessionService.getSessionFromCookie(request.getCookies());
        if (!sessionService.isSessionValid(userSession)) {
            return "redirect:/login";
        }
        User currentUser = userSession.getUser();
        List<Location> locations = locationRepository.findAllByUserId(currentUser.getId());
        request.setAttribute("savedWeathers", locations.stream()
                .map(loc -> new SavedWeatherDto(
                                        loc.getId(),
                        weatherService.findWeatherByLatAndLon(loc.getLatitude(), loc.getLongitude()))
                )
                .collect(Collectors.toList())
        );
        request.setAttribute("login", currentUser.getLogin());
        return "weather";
    }

    // TODO: добавить поддержку поиска городов с одинаковыми названиями
    @PostMapping("/weather")
    public String post(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        UserSession userSession = sessionService.getSessionFromCookie(request.getCookies());
        if (!sessionService.isSessionValid(userSession)) {
            return "redirect:/login";
        }

        String city = request.getParameter("city");
        if (city == null || city.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_CITY);
            return "redirect:/weather";
        }

        OpenWeatherDto dto;
        try {
            dto = weatherService.findWeatherByCity(city);
        } catch (HttpClientErrorException.NotFound e) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.CITY_NOT_FOUND);
            return "redirect:/weather";
        } catch (ResourceAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.TIMEOUT);
            return "redirect:/weather";
        }

        redirectAttributes.addFlashAttribute("foundWeather", dto);
        return "redirect:/weather";
    }

    @PostMapping("/saveWeather")
    public String save(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        UserSession userSession = sessionService.getSessionFromCookie(request.getCookies());
        if (!sessionService.isSessionValid(userSession)) {
            return "redirect:/login";
        }

        String latitude = request.getParameter("lat");
        String longitude = request.getParameter("lon");
        Optional<Location> saveLocation = weatherService.buildLocation(userSession.getUser(), latitude, longitude);
        if (saveLocation.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage.INVALID_CITY);
            return "redirect:/weather";
        }

        locationRepository.save(saveLocation.get());
        return "redirect:/weather";
    }

    @PostMapping("/deleteWeather")
    public String delete(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        UserSession userSession = sessionService.getSessionFromCookie(request.getCookies());
        if (!sessionService.isSessionValid(userSession)) {
            return "redirect:/login";
        }

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

        locationRepository.delete(deleteLocation);
        return "redirect:/weather";
    }
}
