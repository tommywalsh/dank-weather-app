package su.thepeople.weather;

import java.io.Serializable;
import java.time.LocalDateTime;

public class WeatherReport implements Serializable {
    public static class Conditions implements Serializable {
        public LocalDateTime when;
        public double temperature;
        public double dewpoint;
        public double windSpeed;
        public double windDirection;
        public double clouds;
    }

    public Conditions current = new Conditions();
}
