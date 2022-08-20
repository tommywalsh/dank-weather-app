package su.thepeople.weather;

import android.util.Log;

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
 *
 */
public class WeatherFetcher {

    private boolean isWaiting = false;
    private final MessagePasser messagePasser;
    private final String apiKey;

    public WeatherFetcher(MessagePasser messagePasser, String apiKey) {
        this.messagePasser = messagePasser;
        this.apiKey = apiKey;
    }

    private URL getWeatherURL() {
        return URLBuilder
                .url()
                .withHttpsAddress("https://api.openweathermap.org/data/3.0/onecall")
                .withParam("units", "imperial")
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
        } catch (JSONException e) {
            // Malformed result!
        }
        return report;
    }

    private void onFetchCompleted(JSONObject fetchResult) {
        isWaiting = false;
        Log.d("DEBUGGER", fetchResult.toString());
        WeatherReport report = processResult(fetchResult);
        Log.d("report from fetcher", Double.toString(report.current.temperature));
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
