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

public class HourlyWeatherFragment extends Fragment {

    /*
     * This class uses a "RecyclerView" to show a list of hourly forecasts. The RecyclerView is
     * a new-ish Android widget that replaces the old ListView -- it's designed to be more efficient
     * with large lists.
     *
     * To avoid confusion, here's some definitions:
     *   "RecyclerView" is the UI component that handles displaying the list.
     *   "Item view" will mean whatever main UI component is used for a single "slot" in the
     *      RecyclerView's on-screen list. An item view typically contains a handful of widgets.
     *   "Data item" will be the actual data associated with one slot in the list.
     *   "Dataset" will be the entire collection of data items.
     *
     * In our case, an item view is a horizontal layout with a few widgets (one for time, one for
     * temperature, etc.). A data item is one single hourly forecast. The dataset is the most-
     * recently-received WeatherReport.
     *
     * So, rather than create an item view for every single data item, RecyclerView only creates
     * enough item views to fill what is visible on the screen.  When an item view scrolls off the
     * top of the screen, it is repositioned at the bottom, and it is re-bound to a different data
     * item.  So, if you have a dataset with 1000 data items, you don't have to waste memory
     * creating 1000 item views. Instead, you only need enough item views to fit the screen (maybe
     * 15 or so).
     *
     * Our datasets are actually small. So, this whole scheme is a lot more complicated than is
     * necessary for this app.  However, this is now the recommended/supported way to do lists in
     * Android, so here we are.
     *
     * There are three main parts that we have to worry about to get this to work:
     *
     * 1) We have to define a "ViewHolder" class which knows how to access the widget(s)
     *   associated with some specific item view.  In our case, this is the HourlyWeatherViewHolder,
     *   and it knows how to access the widgets that show time, temperature, and so on.
     *
     * 2) We have to define an "Adapter" class which makes the connection between the dataset and
     *   the item views. In our case, this is the HourlyWeatherAdapter. It knows how to
     *   access the dataset, and it knows how to use a ViewHolder to change the values in a
     *   particular item view to match some particular data item.
     *
     * 3) Of course, we need to set up a "RecyclerView" object and tell it to use our custom
     *   Adapter and ViewHolder classes.
     *
     */

    /**
     * Our custom ViewHolder is very simple: it just keeps a reference to each UI widget that might
     * need to be updated.
     */
    private static class HourlyWeatherViewHolder extends RecyclerView.ViewHolder {
        public TextView hourWidget;
        public TextView tempWidget;
        public TextView dewpointWidget;
        public TextView weatherSummaryWidget;
        public TextView weatherDetailsWidget;

        public HourlyWeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            hourWidget = itemView.findViewById(R.id.hourlyTime);
            tempWidget = itemView.findViewById(R.id.hourlyTemp);
            dewpointWidget = itemView.findViewById(R.id.hourlyDewpt);
            weatherSummaryWidget = itemView.findViewById(R.id.hourly_weather_summary);
            weatherDetailsWidget = itemView.findViewById(R.id.hourly_weather_details);
        }
    }

    /**
     * Our custom Adapter class handles connecting the RecyclerView up to our dataset.  Our dataset
     * is the WeatherData object (which holds the most-recently-received weather report).  We'll
     * use our custom ViewHolder class to update item views as necessary.
     */
    private class HourlyWeatherAdapter extends RecyclerView.Adapter<HourlyWeatherViewHolder> {

        /**
         * When the RecyclerView starts up (and maybe at other times during its lifecycle), it will
         * need to create an item view in order to fill one "slot" in the displayed list.
         *
         * Whenever it does this, this method will be called to say "Please create an appropriate
         * item view object, and then wrap it in one of your custom ViewHolder objects".
         */
        @NonNull
        @Override
        public HourlyWeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly_weather_list_item, parent, false);
            return new HourlyWeatherViewHolder(v);
        }

        /**
         * This method will be called by the RecyclerView in two cases:
         *    1) The RecyclerView has just created an item view, and needs to link it to a data
         *      item.
         *    2) The RecyclerView has decided to re-assign an existing item view to a different data
         *      item.
         *
         *  In either case, our job is to simply fill in each of the item view's widget with the
         *  correct data.
         */
        @Override
        public void onBindViewHolder(@NonNull HourlyWeatherViewHolder holder, int position) {
            WeatherReport report = WeatherData.latestReport().getValue();
            WeatherReport.Forecast forecast = (report == null) ? new WeatherReport.Forecast() : report.hourly.get(position);

            holder.hourWidget.setText(Utils.getHourString(forecast.when));
            holder.tempWidget.setText(Utils.getTemperatureString(forecast.temperature));
            holder.dewpointWidget.setText(Utils.getDewpointStringId(forecast.dewpoint));

            int detailId = getResources().getIdentifier(Utils.getWeatherCodeLookupString(forecast.weatherCode), "string", getContext().getPackageName());
            int groupId = getResources().getIdentifier(Utils.getWeatherCodeGroupLookupString(forecast.weatherCode), "string", getContext().getPackageName());
            if (detailId == 0 || groupId == 0) {
                holder.weatherSummaryWidget.setText("");
                holder.weatherDetailsWidget.setText("");
            } else {
                holder.weatherSummaryWidget.setText(Utils.getGeneralWeatherDescriptionId(HourlyWeatherFragment.this, forecast.weatherCode, forecast.clouds));
                holder.weatherDetailsWidget.setText(Utils.getWeatherDetailsString(HourlyWeatherFragment.this, forecast.weatherCode, forecast.pop));
            }
        }

        /**
         * This answers "How many data items are there in the dataset?"
         */
        @Override
        public int getItemCount() {
            WeatherReport latestReport = WeatherData.latestReport().getValue();
            return (latestReport == null) ? 0 : Math.min(latestReport.hourly.size(), 23);
        }
    }

    public HourlyWeatherFragment() {
        // Required empty public constructor
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.fragment_hourly_weather, container, false);

        RecyclerView listView = mainView.findViewById(R.id.hourlyList);

        HourlyWeatherAdapter adapter = new HourlyWeatherAdapter();
        listView.setAdapter(adapter);

        /*
         * notifyDataSetChanged is a big hammer: it says "throw away all data and reload from scratch".
         * In many situations, it's better to use more specific notifications, like "data item #15 changed"
         * or "a new data item appeared between #44 and #45".
         *
         * However, the big hammer is appropriate here. Most updates really will change the whole list.
         * And, in any case, our list is small, so even if we could theoretically be more efficient in
         * some cases, it would be fairly complex and error-prone to do, and the speed improvement
         * wouldn't really be noticeable by a human anyway.
         */
        WeatherData.latestReport().observe(getViewLifecycleOwner(), report -> adapter.notifyDataSetChanged());

        return mainView;
    }
}