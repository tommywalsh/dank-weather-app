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

        WeatherPagerAdapter weatherPagerAdapter = new WeatherPagerAdapter(this, getSupportFragmentManager(), weatherReport);
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

    @Override
    protected void onDestroy() {
        modelStore.clear();
        super.onDestroy();
    }

    private void onNewWeatherReportReceived(WeatherReport newReport) {
        Log.d("main", Double.toString(newReport.current.temperature));
        weatherReport.setValue(newReport);
        WeatherData.latestReport().setValue(newReport);
        Log.d("main after", Double.toString(newReport.current.temperature));
    }
}