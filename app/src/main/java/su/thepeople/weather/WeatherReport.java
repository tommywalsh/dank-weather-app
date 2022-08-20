package su.thepeople.weather;

import java.io.Serializable;

public class WeatherReport implements Serializable {
    public static class CurrentWeather implements Serializable {
        public double currentTemp;
        public double currentDewpoint;
    }

    public CurrentWeather current = new CurrentWeather();
}
