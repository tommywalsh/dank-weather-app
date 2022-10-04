package su.thepeople.weather.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import su.thepeople.weather.R;
import su.thepeople.weather.WeatherData;
import su.thepeople.weather.WeatherReport;

public class CurrentWeatherFragment extends Fragment {

    public CurrentWeatherFragment() {
        // Required empty public constructor
    }

    private TextView tempWidget;
    private TextView weatherSummaryWidget;
    private TextView weatherDetailsWidget;

    private void updateWidgets(WeatherReport report) {
        if (report == null) {
            tempWidget.setText("");
            weatherSummaryWidget.setText("");
            weatherDetailsWidget.setText("");
            return;
        }
        tempWidget.setText(Utils.getTemperatureString(report.current.temperature));

        int groupId = Utils.getWeatherGroupCode(report.current.weatherCode);

        if (groupId == 300) {
            // For Drizzle, we don't need to know details
            weatherSummaryWidget.setText(Utils.getResourceId(this, Utils.getWeatherCodeGroupLookupString(report.current.weatherCode)));
            weatherDetailsWidget.setText("");
        } else {
            // For every other weather type, we want details
            weatherSummaryWidget.setText(Utils.getResourceId(this, Utils.getWeatherCodeLookupString(report.current.weatherCode)));

            if (groupId == 600) {
                // For snow, we want to know accumulation for the day
                double snowTotal = 0.0;
                if (!report.daily.isEmpty()) {
                    snowTotal = report.daily.get(0).snowAccumulation;
                }
                weatherDetailsWidget.setText(Utils.getSnowString(snowTotal)); // TODO: where to get this data?
            } else if (groupId == 800) {
                // If no precipitation, we want to know about humidity
                weatherDetailsWidget.setText(Utils.getDewpointStringId(report.current.dewpoint));
            } else {
                // Otherwise, no additional info is needed
                weatherDetailsWidget.setText("");
            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_current_weather, container, false);

        tempWidget = inflatedView.findViewById(R.id.current_temp);
        weatherSummaryWidget = inflatedView.findViewById(R.id.current_weather_summary);
        weatherDetailsWidget = inflatedView.findViewById(R.id.current_weather_details);

        WeatherData.latestReport().observe(getViewLifecycleOwner(), this::updateWidgets);

        return inflatedView;
    }
}