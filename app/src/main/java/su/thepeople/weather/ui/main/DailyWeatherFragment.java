package su.thepeople.weather.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import su.thepeople.weather.R;
import su.thepeople.weather.WeatherData;
import su.thepeople.weather.WeatherReport;

public class DailyWeatherFragment extends Fragment {

    /*
     * Most of the code in this class handles the complex setup of the RecyclerView widget. Please
     * see comments in HourlyWeatherFragment for full details (this class is a direct analogue to
     * that one).
     */
    private static class DailyWeatherViewHolder extends RecyclerView.ViewHolder {
        public TextView dayWidget;
        public TextView dayTempWidget;
        public TextView highLowWidget;
        public TextView dewpointWidget;
        public TextView summaryWidget;
        public TextView popWidget;

        public DailyWeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            dayWidget = itemView.findViewById(R.id.dailyLabel);
            dayTempWidget = itemView.findViewById(R.id.dailyDayTemp);
            highLowWidget = itemView.findViewById(R.id.dailyHighLowTemp);
            dewpointWidget = itemView.findViewById(R.id.dailyDewpt);
            summaryWidget = itemView.findViewById(R.id.daily_condition_summary);
            popWidget = itemView.findViewById(R.id.daily_pop);
        }
    }

    private class DailyWeatherAdapter extends RecyclerView.Adapter<DailyWeatherViewHolder> {
        @NonNull
        @Override
        public DailyWeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_weather_list_item, parent, false);
            return new DailyWeatherViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DailyWeatherViewHolder holder, int position) {
            WeatherReport report = WeatherData.latestReport().getValue();
            WeatherReport.DailyForecast forecast = (report == null) ? new WeatherReport.DailyForecast() : report.daily.get(position);

            holder.dayWidget.setText(Utils.getDayString(forecast.when));
            holder.dayTempWidget.setText(Utils.getTemperatureString(forecast.temperature));
            holder.highLowWidget.setText(Utils.getHighLowTemperatureString(forecast.highTemperature, forecast.lowTemperature));
            holder.popWidget.setText(Utils.getPopString(DailyWeatherFragment.this, forecast.pop));

            int groupId = Utils.getWeatherGroupCode(forecast.weatherCode);
            if (groupId == 800) {
                // For non-precipitation, we give the detailed condition (the weather code)
                holder.summaryWidget.setText(Utils.getResourceId(DailyWeatherFragment.this, Utils.getWeatherCodeLookupString(forecast.weatherCode)));
            } else {
                // Otherwise, just a general description is good (the weather group)
                holder.summaryWidget.setText(Utils.getResourceId(DailyWeatherFragment.this, Utils.getWeatherCodeGroupLookupString(forecast.weatherCode)));
            }

            if (groupId == 800) {
                // For non-precipitation, we give the dewpoint
                holder.dewpointWidget.setText(Utils.getDewpointStringId(forecast.dewpoint));
            } else if (groupId == 600) {
                // For snow days, we give the accumulation
                holder.dewpointWidget.setText(Utils.getSnowString(forecast.snowAccumulation));
            } else {
                holder.dewpointWidget.setText("");
            }
        }

        @Override
        public int getItemCount() {
            WeatherReport latestReport = WeatherData.latestReport().getValue();
            return (latestReport == null) ? 0 : Math.min(latestReport.daily.size(), 7);
        }
    }

    public DailyWeatherFragment() {
        // Required empty public constructor
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_daily_weather, container, false);

        RecyclerView listView = mainView.findViewById(R.id.dailyList);

        DailyWeatherAdapter adapter = new DailyWeatherAdapter();
        listView.setAdapter(adapter);

        WeatherData.latestReport().observe(getViewLifecycleOwner(), report -> adapter.notifyDataSetChanged());

        return mainView;
    }
}