package su.thepeople.weather;

import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.time.Duration;
import java.time.LocalDateTime;

public class DataLifecycleManager {

    private static final String L = "DataLifecycleManager";

    private static final Duration LOCATION_LIFETIME = Duration.ofHours(1);
    private static final Duration WEATHER_LIFETIME = Duration.ofMinutes(10);

    private final LocationFetcher locationFetcher;
    private final WeatherFetcher weatherFetcher;
    private final MessagePasser messagePasser;

    private SimpleLocation workingLocation;
    private WeatherReport workingReport;

    public DataLifecycleManager(MessagePasser messagePasser, String apiKey, FusedLocationProviderClient locationProvider, Geocoder geocoder) {
        this.locationFetcher = new LocationFetcher(locationProvider, geocoder, this::onLocationReceived);
        this.weatherFetcher = new WeatherFetcher(apiKey, this::onWeatherReportReceived);
        this.messagePasser = messagePasser;
    }

    private static boolean timeDiffIsAboveLimit(LocalDateTime olderTime, LocalDateTime newerTime, Duration limit) {
        Duration timeDiff = Duration.between(olderTime, newerTime);
        int comparison = timeDiff.compareTo(limit);
        return comparison > 0;
    }

    public void fullRefresh() {
        Log.d("WeatherFetcher", "full update requested");

        // Consider any location we might already know to be outdated.
        workingLocation = null;

        // Try to learn our location.
        locationFetcher.dispatchRequests();

        // While we're waiting for location responses, ask for weather for our last-cached location if we have one.
        if (workingLocation != null) {
            weatherFetcher.dispatchRequest(workingLocation);
        }
    }

    /*
     * Returns the amount of time that it is suggested to wait for the next update.
     */
    public Duration refreshAsNecessary() {
        Log.d(L, "Checking to see if an update is necessary");

        Duration nextUpdateWait = WEATHER_LIFETIME;
        LocalDateTime now = LocalDateTime.now();

        // If we have no known location, then do a full update
        if (workingLocation == null || workingLocation.type == SimpleLocation.LocationType.OUTDATED) {
            Log.d(L, "We do not have a location. Doing a full update");
            fullRefresh();
        } else if (timeDiffIsAboveLimit(workingLocation.timestamp, now, LOCATION_LIFETIME)) {
            Log.d(L, "Our existing location is too old. Requesting a location update");
            locationFetcher.dispatchRequests();
        } else if (workingReport == null) {
            Log.d(L, "Our existing location is up-to-date, but we have no weather report. Asking for new report.");
            weatherFetcher.dispatchRequest(workingLocation);
        } else if (timeDiffIsAboveLimit(workingReport.getUpdateTime(), now, WEATHER_LIFETIME)) {
            Log.d(L, "Our existing location is up-to-date, but the weather report is old. Asking only for new forecast.");
            weatherFetcher.dispatchRequest(workingLocation);
        } else {
            Log.d(L, "Location and forecast are both up-to-date. No need to update.");

            Duration age = Duration.between(workingReport.getUpdateTime(), now);
            nextUpdateWait = WEATHER_LIFETIME.minus(age);
        }
        return nextUpdateWait;
    }

    private void updateToNewLocation(SimpleLocation newLocation) {
        workingLocation = newLocation;

        // any time we update our location, we should check for the weather.
        weatherFetcher.dispatchRequest(newLocation);
    }

    private void onLocationReceived(SimpleLocation newLocation) {
        Log.d(L, "New location received");
        if (workingLocation == null) {
            Log.d(L, "No prior location, using new one");
            updateToNewLocation(newLocation);
        } else if (workingLocation.type == SimpleLocation.LocationType.OUTDATED) {
            Log.d(L, "Prior location is out of date, using new one");
            updateToNewLocation(newLocation);
        } else if (workingLocation.timestamp.isBefore(newLocation.timestamp)) {
            Log.d(L, "Received location is newer than the one we have. Updating");
            updateToNewLocation(newLocation);
        } else {
            Log.d(L, "Received location is older than the one we have. Ignoring");
        }
    }

    private void onWeatherReportReceived(WeatherReport report) {
        workingReport = report;
        messagePasser.sendNewWeatherReport(report);
    }
}
