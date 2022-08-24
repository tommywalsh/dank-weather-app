package su.thepeople.weather;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelStore;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.util.Log;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import su.thepeople.weather.ui.main.WeatherPagerAdapter;
import su.thepeople.weather.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private WeatherFetcher fetcher;

    MutableLiveData<WeatherReport> weatherReport = new MutableLiveData<>();

    ViewModelStore modelStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force creation of singleton.  Maybe not necessary
        WeatherData.latestReport();

        modelStore = new ViewModelStore();

        su.thepeople.weather.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WeatherPagerAdapter weatherPagerAdapter = new WeatherPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(weatherPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

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
        fetcher = new WeatherFetcher(passer, apiKey);

        fab.setOnClickListener(view -> fetcher.requestWeatherUpdate());
    }

    private Timer weatherReportTimer = new Timer();

    private void requestNewReport() {
        // Regardless of WHY we're being asked to request a new report, we need to:
        // 1) Cancel any pending timer events
        // 2) Actually request a new report
        // 3) Start a new timer
    }
    private Duration TOO_OLD = Duration.ofHours(3);
    private Duration UP_TO_DATE = Duration.ofMinutes(5);

    @Override public void onResume() {
        super.onResume();

        // Four cases:
        //   1) We don't have a report at all
        //   2) We have a report, but it is very old
        //   3) We have a report which is not up-to-date, but is "new enough"
        //   4) We have an up-to-date report
        WeatherReport latestReport = WeatherData.latestReport().getValue();

        boolean makeRequest = false;
        boolean throwAway = false;
        if (latestReport == null) {
            // Case 1
            makeRequest = true;
        } else {
            Duration age = Duration.between(latestReport.getUpdateTime(), LocalDateTime.now());
            if (age.compareTo(TOO_OLD) > 0) {
                // Case 2
                makeRequest = true;
                throwAway = true;
            } else if (age.compareTo(UP_TO_DATE) > 0) {
                // Case 3
                makeRequest = true;
            }
            // Nothing to do for Case 4
        }
        if (throwAway) {
            WeatherData.latestReport().setValue(null);
        }
        if (makeRequest) {
            fetcher.requestWeatherUpdate();
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                fetcher.requestWeatherUpdate();
            }
        };
        weatherReportTimer.schedule(task, 0, UP_TO_DATE.toMillis());
    }

    @Override protected void onPause() {
        weatherReportTimer.cancel();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        modelStore.clear();
        super.onDestroy();
    }

    private void onNewWeatherReportReceived(WeatherReport newReport) {
        weatherReport.setValue(newReport);
        WeatherData.latestReport().setValue(newReport);
    }
}