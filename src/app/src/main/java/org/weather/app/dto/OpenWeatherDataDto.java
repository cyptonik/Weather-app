package org.weather.app.dto;

import java.math.BigDecimal;
import java.util.List;

public class OpenWeatherDataDto {
    public Coord coord;
    public String name;
    public List<Weather> weather;
    public Main main;

    public static class Weather {
       public String description;
       public String icon;
    }

    public static class Coord {
        public BigDecimal lon;
        public BigDecimal lat;
    }

    public static class Main {
        public BigDecimal temp;
    }
}
