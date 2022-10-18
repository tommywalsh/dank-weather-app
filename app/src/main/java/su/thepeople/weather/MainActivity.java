package su.thepeople.weather;

import android.location.Geocoder;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;

import androidx.lifecycle.MutableLiveData;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.time.Duration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import su.thepeople.weather.ui.main.WeatherPagerAdapter;
import su.thepeople.weather.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String L = "MainActivity";

    private DataLifecycleManager fetcher;
    private TextView titleWidget;

    MutableLiveData<WeatherReport> weatherReport = new MutableLiveData<>();

    private Timer dataRefreshTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "creating");

        su.thepeople.weather.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        titleWidget = binding.title;

        // Set up our tabbed pager
        WeatherPagerAdapter weatherPagerAdapter = new WeatherPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(weatherPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        // Utility to let other threads pass messages to us.
        MessagePasser passer = new MessagePasser(Looper.getMainLooper(), this::onNewWeatherReportReceived);

        /*
         * The WeatherFetcher requires an API key.  This API key must be stored in a resource file that is **NOT** checked into source control.
         * In order to compile this file, you'll need to create an XML resource file, and put your API key in that file, as a string resource
         * with the name "weather_api_key". For example, you could create a file named "secrets.xml" and make the contents look like this:
         *
         * <resources>
         *     <string name="weather_api_key">01234356789abcdef</string>
         * </resources>
         */
        String apiKey = getResources().getString(R.string.weather_api_key);

        /*
         * Set up the data fetcher
         */
        FusedLocationProviderClient locationProvider = LocationServices.getFusedLocationProviderClient(this);
        Geocoder geocoder = new Geocoder(this, Locale.US);
        fetcher = new DataLifecycleManager(passer, apiKey, locationProvider, geocoder);
    }

    private static TimerTask taskOf(Runnable r) {
        return new TimerTask() {
            public void run() {
                r.run();
            }
        };
    }

    private void requestDataUpdate() {
        Duration updateWaitTime = fetcher.refreshAsNecessary();
        Log.d(L, "Wait time " + updateWaitTime.toString());
        dataRefreshTimer.schedule(taskOf(this::requestDataUpdate), updateWaitTime.toMillis());
    }

    @Override public void onResume() {
        super.onResume();

        Log.d("MainActivity", "resuming");
        dataRefreshTimer = new Timer();
        requestDataUpdate();
    }

    @Override protected void onPause() {
        dataRefreshTimer.cancel();
        dataRefreshTimer = null;
        Log.d("MainActivity", "pausing");
        super.onPause();
    }

    private void onNewWeatherReportReceived(WeatherReport newReport) {
        Log.d("MainActivity", "received weather report");
        titleWidget.setText(newReport.locationName);
        weatherReport.setValue(newReport);
        WeatherData.latestReport().setValue(newReport);
    }
}