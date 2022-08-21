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
    private TextView dewpointWidget;
    private TextView cloudWidget;

    private void updateWidgets(WeatherReport report) {
        tempWidget.setText(Utils.getTemperatureString(report.current.temperature));
        dewpointWidget.setText(getResources().getString(Utils.getDewpointStringId(report.current.dewpoint)));
        cloudWidget.setText(getResources().getString(Utils.getCloudinessStringId(report.current.clouds)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_current_weather, container, false);

        tempWidget = inflatedView.findViewById(R.id.current_temp);
        dewpointWidget = inflatedView.findViewById(R.id.current_dewpt);
        cloudWidget = inflatedView.findViewById(R.id.current_clouds);

        WeatherData.latestReport().observe(getViewLifecycleOwner(), this::updateWidgets);

        return inflatedView;
    }
}