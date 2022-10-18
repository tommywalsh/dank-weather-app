package su.thepeople.weather;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WeatherFetcher {
    private static final String L = "WeatherFetcher";

    private final String apiKey;
    private final static String API_ADDRESS = "https://api.openweathermap.org/data/3.0/onecall";
    private final static String IMPERIAL_UNITS = "imperial";

    private final Consumer<WeatherReport> reportCallback;

    public WeatherFetcher(String apiKey, Consumer<WeatherReport> reportCallback) {
        this.apiKey = apiKey;
        this.reportCallback = reportCallback;
    }

    private URL getWeatherURL(SimpleLocation location) {
        return URLBuilder
                .url()
                .withHttpsAddress(API_ADDRESS)
                .withParam("units", IMPERIAL_UNITS)
                .withParam("appid", apiKey)
                .withParam("lat", String.format(Locale.US, "%f", location.lat))
                .withParam("lon", String.format(Locale.US, "%f", location.lng))
                .build();
    }

    private Supplier<JSONObject> apiCaller(SimpleLocation location) {
        return () -> {
            try {
                return NetworkCaller.getJSONObjectResult(getWeatherURL(location));
            } catch (IOException | JSONException e) {
                throw new CompletionException(e);
            }
        };
    }

    private void onWeatherResponse(JSONObject weatherResponse, SimpleLocation location) {
        Log.d(L, "New weather forecast received.");
        WeatherReport report = processResult(weatherResponse, location);
        reportCallback.accept(report);
    }

    public void dispatchRequest(SimpleLocation location) {
        Log.d(L, "Forecast request underway");
        CompletableFuture
                .supplyAsync(apiCaller(location))
                .thenAccept((report) -> onWeatherResponse(report, location))
                .exceptionally(e -> {
                    Log.e(L, "Error getting forecast", e);
                    return null;
                });
    }

    private WeatherReport processResult(JSONObject result, SimpleLocation location) {
        WeatherReport report = new WeatherReport();
        try {

            // Data that applies to all times
            int offsetL = result.getInt("timezone_offset");
            ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetL);
            report.location = location;
            report.locationName = location.name;

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

                // TODO: This JSON appears in practice to always be in chronological order. Sort it to be sure?
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
            Log.e("WeatherFetcher", "malformed JSON from server", e);
        }
        return report;
    }
}
