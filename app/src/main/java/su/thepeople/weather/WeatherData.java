package su.thepeople.weather;

import androidx.annotation.MainThread;
import androidx.lifecycle.MutableLiveData;

public class WeatherData extends MutableLiveData<WeatherReport> {
    private static WeatherData s_instance;

    @MainThread
    public static WeatherData latestReport() {
        if (s_instance == null) {
            s_instance = new WeatherData();
        }
        return s_instance;
    }
}
