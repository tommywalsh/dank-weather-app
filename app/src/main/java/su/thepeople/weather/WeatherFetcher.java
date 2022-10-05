package su.thepeople.weather;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;

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
import java.util.function.Supplier;

/**
 * Fetches weather reports from an external server.
 */
public class WeatherFetcher {

    private boolean isWaitingForExternallyRequestedUpdate = false;
    private final MessagePasser messagePasser;
    private final String apiKey;

    private final static String API_ADDRESS = "https://api.openweathermap.org/data/3.0/onecall";
    private final static String IMPERIAL_UNITS = "imperial";

    private final FusedLocationProviderClient locationProvider;

    private WeatherReport.LatLng currentLocation = null;
    private final CancellationTokenSource cancelSource = new CancellationTokenSource();

    public WeatherFetcher(MessagePasser messagePasser, String apiKey, FusedLocationProviderClient locationProvider) {
        this.messagePasser = messagePasser;
        this.apiKey = apiKey;
        this.locationProvider = locationProvider;
    }

    private URL getWeatherURL() {
        assert currentLocation != null;

        return URLBuilder
                .url()
                .withHttpsAddress(API_ADDRESS)
                .withParam("units", IMPERIAL_UNITS)
                .withParam("appid", apiKey)
                .withParam("lat", String.format(Locale.US, "%f", currentLocation.lat))
                .withParam("lon", String.format(Locale.US, "%f", currentLocation.lng))
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
            report.location = currentLocation;

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
        Log.d("WeatherFetcher", "weather report received");

        isWaitingForExternallyRequestedUpdate = false;
        WeatherReport report = processResult(fetchResult);
        messagePasser.sendNewWeatherReport(report);
    }

    private void onLocationRetrieved(Location location) {
        Log.d("WeatherFetcher", "location received, will request weather report now");

        currentLocation = new WeatherReport.LatLng(location.getLatitude(), location.getLongitude());
        requestWeatherUpdate();
    }

    private void onLocationFailed(Exception e) {
        Log.d("debug", "Location failed", e);
    }

    private void requestLocation() {
        Log.d("WeatherFetcher", "location requested internally");

        try {
            Log.d("WeatherFetcher", "asking provider for location");

            Task<Location> locationTask = locationProvider.getCurrentLocation(LocationRequest.PRIORITY_LOW_POWER, cancelSource.getToken());
            locationTask.addOnSuccessListener(this::onLocationRetrieved);
            locationTask.addOnFailureListener(this::onLocationFailed);
        } catch(SecurityException e) {
            // If we can't get the real location, use Somerville as a default, and request a new weather update.
            Log.d("WeatherFetcher", "security exception received on location call");

            currentLocation = new WeatherReport.LatLng(42.379, -71.0998);
            CompletableFuture.runAsync(this::requestWeatherUpdate);
        } catch (Exception e) {{
            Log.d("WeatherFetcher", "another exception received on location call", e);
        }}
    }

    public void fullUpdate() {
        Log.d("WeatherFetcher", "full update requested");

        // A full update means we might have changed location.  Fire off a location request, which will in turn update the weather.
        requestLocation();
    }

    public void updateWeatherForLastLocation() {
        Log.d("WeatherFetcher", "update requested for last location");

        // In this case, we assume the device is at the same location it was before (if indeed there was a "before").
        if (isWaitingForExternallyRequestedUpdate) {
            // We're already requesting weather. Don't send a second request.
            Log.d("WeatherFetcher", "we're already waiting for a report, so ignore this request");

            return;
        }
        isWaitingForExternallyRequestedUpdate = true;
        requestWeatherUpdate();
    }

    private void requestWeatherUpdate() {
        Log.d("WeatherFetcher", "generic update requested internally");

        if (currentLocation == null) {
            Log.d("WeatherFetcher", "generic update is for location since we don't have one yet");

            // We have not determined an initial location yet!  Request it, and that will automatically fetch a new weather update afterwards.
            requestLocation();
        } else {
            Log.d("WeatherFetcher", "dispatching request to weather API async");

            CompletableFuture
                    .supplyAsync(apiCaller())
                    .thenAccept(this::onFetchCompleted)
                    .exceptionally(e -> {
                        Log.d("WeatherFetcher", "weather API call exception", e);

                        isWaitingForExternallyRequestedUpdate = false;
                        return null;
                    });
        }
    }
}
