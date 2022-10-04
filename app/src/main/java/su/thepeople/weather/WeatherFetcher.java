package su.thepeople.weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

/**
 * Fetches weather reports from an external server.
 */
public class WeatherFetcher {

    private boolean isWaiting = false;
    private final MessagePasser messagePasser;
    private final String apiKey;

    private final static String API_ADDRESS = "https://api.openweathermap.org/data/3.0/onecall";
    private final static String IMPERIAL_UNITS = "imperial";

    public WeatherFetcher(MessagePasser messagePasser, String apiKey) {
        this.messagePasser = messagePasser;
        this.apiKey = apiKey;
    }

    private URL getWeatherURL() {
        return URLBuilder
                .url()
                .withHttpsAddress(API_ADDRESS)
                .withParam("units", IMPERIAL_UNITS)
                .withParam("appid", apiKey)

                // TODO: Don't hard-code Somerville!
                .withParam("lat", "42.379")
                .withParam("lon", "-71.0998")

                .build();
    }

    private Supplier<JSONObject> apiCaller() {
        return () -> {
            try {
                return NetworkCaller.getJSONObjectResult(getWeatherURL());
            } catch (IOException | JSONException e) {
                throw new CompletionException(e);
            }
        };
    }

    private WeatherReport processResult(JSONObject result) {
        WeatherReport report = new WeatherReport();
        try {

            // Data that applies to all times
            int offsetL = result.getInt("timezone_offset");
            ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetL);

            // Data that applies to current conditions
            JSONObject current = result.getJSONObject("current");
            report.current.when = LocalDateTime.ofEpochSecond(current.getLong("dt"), 0, offset);
            report.current.temperature = current.getDouble("temp");
            report.current.dewpoint = current.getDouble("dew_point");
            report.current.clouds = current.getDouble("clouds");
            report.current.windSpeed = current.getDouble("wind_speed");
            report.current.windDirection = current.getDouble("wind_deg");
            JSONArray currentWeather = current.getJSONArray("weather");
            if (currentWeather.length() > 0) {
                JSONObject primaryWeather = currentWeather.getJSONObject(0);
                report.current.weatherCode = primaryWeather.getInt("id");
            }

            // Data that applies to hourly forecasts
            JSONArray hourly = result.getJSONArray("hourly");
            for (int i = 0; i < hourly.length(); ++i) {
                JSONObject thisHour = hourly.getJSONObject(i);
                WeatherReport.Forecast thisForecast = new WeatherReport.Forecast();
                thisForecast.when = LocalDateTime.ofEpochSecond(thisHour.getLong("dt"), 0, offset);
                thisForecast.temperature = thisHour.getDouble("temp");
                thisForecast.dewpoint = thisHour.getDouble("dew_point");
                thisForecast.clouds = thisHour.getDouble("clouds");
                thisForecast.windSpeed = thisHour.getDouble("wind_speed");
                thisForecast.windDirection = thisHour.getDouble("wind_deg");
                thisForecast.pop = thisHour.getDouble("pop");
                JSONArray hourlyWeather = thisHour.getJSONArray("weather");
                if (hourlyWeather.length() > 0) {
                    JSONObject primaryWeather = hourlyWeather.getJSONObject(0);
                    thisForecast.weatherCode = primaryWeather.getInt("id");
                }

                // TODO: Check to see if this JSON array is really always in chronological order!
                report.hourly.add(thisForecast);
            }

            // Data that applies to daily forecasts
            JSONArray daily = result.getJSONArray("daily");
            for (int i = 0; i < daily.length(); ++i) {
                JSONObject thisDay = daily.getJSONObject(i);
                WeatherReport.DailyForecast thisForecast = new WeatherReport.DailyForecast();
                thisForecast.when = LocalDateTime.ofEpochSecond(thisDay.getLong("dt"), 0, offset);
                JSONObject thisTemp = thisDay.getJSONObject("temp");
                thisForecast.temperature = thisTemp.getDouble("day");
                thisForecast.highTemperature = thisTemp.getDouble("max");
                thisForecast.lowTemperature = thisTemp.getDouble("min");
                thisForecast.dewpoint = thisDay.getDouble("dew_point");
                thisForecast.clouds = thisDay.getDouble("clouds");
                thisForecast.windSpeed = thisDay.getDouble("wind_speed");
                thisForecast.windDirection = thisDay.getDouble("wind_deg");
                thisForecast.pop = thisDay.getDouble("pop");

                // Snow is always given in mm even when we're requesting imperial units
                thisForecast.snowAccumulation = thisDay.has("snow") ? thisDay.getDouble("snow") / 25.4 : 0.0;

                JSONArray dailyWeather = thisDay.getJSONArray("weather");
                if (dailyWeather.length() > 0) {
                    JSONObject primaryWeather = dailyWeather.getJSONObject(0);
                    thisForecast.weatherCode = primaryWeather.getInt("id");
                }

                report.daily.add(thisForecast);
            }
        } catch (JSONException e) {
            // Malformed result!
        }
        return report;
    }

    private void onFetchCompleted(JSONObject fetchResult) {
        isWaiting = false;
        WeatherReport report = processResult(fetchResult);
        messagePasser.sendNewWeatherReport(report);
    }

    public void requestWeatherUpdate() {
        if (isWaiting) {
            // We're already requesting weather. Don't send a second request.
            return;
        }
        isWaiting = true;
        CompletableFuture
            .supplyAsync(apiCaller())
            .thenAccept(this::onFetchCompleted)
            .exceptionally(e -> {
                isWaiting = false;
                return null;
            });
    }
}
