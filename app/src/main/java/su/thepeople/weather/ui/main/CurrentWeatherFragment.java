package su.thepeople.weather.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import su.thepeople.weather.R;
import su.thepeople.weather.WeatherData;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CurrentWeatherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CurrentWeatherFragment extends Fragment {

    public CurrentWeatherFragment() {
        // Required empty public constructor
    }

    public static CurrentWeatherFragment newInstance() {
        CurrentWeatherFragment fragment = new CurrentWeatherFragment();
        // TODO: is this necessary?
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_current_weather, container, false);

        TextView tempWidget = inflatedView.findViewById(R.id.current_temp);
        TextView dewpointWidget = inflatedView.findViewById(R.id.current_dewpt);
        TextView cloudWidget = inflatedView.findViewById(R.id.current_clouds);

        WeatherData.latestReport().observe(getViewLifecycleOwner(), report -> {
            tempWidget.setText(Utils.getTemperatureString(report.current.temperature));
            dewpointWidget.setText(getResources().getString(Utils.getDewpointStringId(report.current.dewpoint)));
            cloudWidget.setText(getResources().getString(Utils.getCloudinessStringId(report.current.clouds)));
        });

        return inflatedView;
    }
}