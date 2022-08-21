package su.thepeople.weather;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WeatherReport implements Serializable {
    public static class Conditions implements Serializable {
        public LocalDateTime when;
        public double temperature;
        public double dewpoint;
        public double windSpeed;
        public double windDirection;
        public double clouds;
    }

    public static class DailyForecast extends Conditions {
        public double lowTemperature;
        public double highTemperature;
    }

    public Conditions current = new Conditions();

    public List<Conditions> hourly = new ArrayList<>();

    public List<DailyForecast> daily = new ArrayList<>();
}
