package su.thepeople.weather;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class LocationFetcher {

    public static final String L = "LocationFetcher";
    private final FusedLocationProviderClient locationProvider;
    private final Consumer<SimpleLocation> locationCallback;
    private final Geocoder geocoder;

    private final CancellationTokenSource cancelSource = new CancellationTokenSource();

    public LocationFetcher(FusedLocationProviderClient locationProvider, Geocoder geocoder, Consumer<SimpleLocation> locationCallback) {
        this.locationProvider = locationProvider;
        this.geocoder = geocoder;
        this.locationCallback = locationCallback;
    }

    private void onLastKnownLocationResult(Location l) {
        if (l != null) {
            locationCallback.accept(new SimpleLocation(l, SimpleLocation.LocationType.LAST_KNOWN, getLocationName(l)));
        }
    }

    private void onCurrentLocationResult(Location l) {
        if (l != null) {
            locationCallback.accept(new SimpleLocation(l, SimpleLocation.LocationType.CURRENT, getLocationName(l)));
        }
    }

    public void dispatchRequests() {
        try {
            Task<Location> locationTask = locationProvider.getLastLocation();
            locationTask.addOnSuccessListener(this::onLastKnownLocationResult);
            locationTask = locationProvider.getCurrentLocation(LocationRequest.PRIORITY_LOW_POWER, cancelSource.getToken());
            locationTask.addOnSuccessListener(this::onCurrentLocationResult);
        } catch (SecurityException e) {
            Log.e(L, "Security exception while asking for location", e);
        }
    }

    /*
     * This method uses a synchronous call to do a reverse geocode. This is not great, but it's all that's available at API level 31.
     * If we ever update this app to API level 33 or higher, we should use the new async interface instead, which would involve breaking this up
     * into separate "dispatchLocationNameRequest" and "onLocationNameResponse" methods.  This will also involve making sure we don't send off a
     * location update event until we have BOTH the new lat/lng AND the location name.
     */
    private String getLocationName(Location location) {
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 40);
        } catch (IOException e) {
            Log.e(L, "IO error during reverse geocoding", e);
        }

        if (addresses != null) {
            for (Address address : addresses) {
                String locality = address.getLocality();
                if (locality != null && !locality.isEmpty()) {
                    return locality;
                }
            }
        }
        return String.format(Locale.US, "lat %f, lng %f", location.getLatitude(), location.getLongitude());
    }
}
