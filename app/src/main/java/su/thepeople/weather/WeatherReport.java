package su.thepeople.weather;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WeatherReport implements Serializable {

    private static LocalDateTime lastUpdateTime;

    public WeatherReport() {
        lastUpdateTime = LocalDateTime.MIN;
    }
    public LocalDateTime getUpdateTime() {
        return lastUpdateTime;
    }

    public static class Conditions implements Serializable {
        public LocalDateTime when;
        public double temperature;
        public double dewpoint;
        public double windSpeed;
        public double windDirection;
        public double clouds;
        public int weatherCode;
    }

    public static class Forecast extends Conditions {
        public double pop;
    }

    public static class DailyForecast extends Forecast {
        public double lowTemperature;
        public double highTemperature;
        public double snowAccumulation;
    }

    public Conditions current = new Conditions();

    public List<Forecast> hourly = new ArrayList<>();

    public List<DailyForecast> daily = new ArrayList<>();
}
