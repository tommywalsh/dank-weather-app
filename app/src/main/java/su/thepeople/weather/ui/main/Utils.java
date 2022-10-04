package su.thepeople.weather.ui.main;

import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import su.thepeople.weather.R;

public class Utils {

    private static final DecimalFormat roundedDecimal = new DecimalFormat("###");

    public static String getTemperatureString(double temperature) {
        return String.format("%s°", roundedDecimal.format(temperature));
    }

    public static String getSnowString(double snowInInches) {
        return String.format("%s\"", roundedDecimal.format(snowInInches));
    }

    public static String getHighLowTemperatureString(double highTemp, double lowTemp) {
        return String.format("%s°/%s°", roundedDecimal.format(lowTemp), roundedDecimal.format(highTemp));
    }

    private static final DateTimeFormatter hourOnlyFormatter = DateTimeFormatter.ofPattern("h a");

    public static String getHourString(LocalDateTime dt) {
        return dt.format(hourOnlyFormatter);
    }

    private static final DateTimeFormatter dayOnlyFormatter = DateTimeFormatter.ofPattern("E");

    public static String getDayString(LocalDateTime dt) { return dt.format(dayOnlyFormatter); }

    public static int getDewpointStringId(double dewpoint) {
        if (dewpoint >= 70.0) {
            return R.string.dew_pt_above_70;
        } else if (dewpoint >= 60.0) {
            return R.string.dew_pt_60s;
        } else if (dewpoint >= 50.0) {
            return R.string.dew_pt_50s;
        } else if (dewpoint >= 40.0) {
            return R.string.dew_pt_40s;
        } else {
            return R.string.dew_pt_below_40;
        }
    }

    public static String getWeatherCodeLookupString(int weatherCode) {
        return String.format(Locale.getDefault(), "weather_code_%d", weatherCode);
    }

    public static int getWeatherGroupCode(int weatherCode) {
        return (weatherCode / 100) * 100;
    }

    public static String getWeatherCodeGroupLookupString(int weatherCode) {
        return String.format(Locale.getDefault(), "weather_group_%d", getWeatherGroupCode(weatherCode));
    }

    public static int getResourceId(Fragment ui, String resourceName) {
        return ui.getResources().getIdentifier(resourceName, "string", ui.requireContext().getPackageName());
    }

    public static String getPopString(Fragment ui, double pop) {
        String popString = ui.getResources().getString(R.string.pop);
        return String.format("%s: %s%%", popString, roundedDecimal.format(pop*100.0));
    }
}
