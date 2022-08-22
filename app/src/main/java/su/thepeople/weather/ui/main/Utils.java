package su.thepeople.weather.ui.main;

import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import su.thepeople.weather.R;

public class Utils {

    private static final DecimalFormat roundedDecimal = new DecimalFormat("###");

    public static String getTemperatureString(double temperature) {
        return String.format("%s°", roundedDecimal.format(temperature));
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
        return String.format("weather_code_%d", weatherCode);
    }

    private static int getWeatherGroupCode(int weatherCode) {
        return (weatherCode / 100) * 100;
    }
    public static String getWeatherCodeGroupLookupString(int weatherCode) {
        return String.format("weather_group_%d", getWeatherGroupCode(weatherCode));
    }

    private static final int NO_PRECIP_GROUP_CODE = 800;

    private static int getResourceId(Fragment ui, String resourceName) {
        return ui.getResources().getIdentifier(resourceName, "string", ui.getContext().getPackageName());
    }

    public static int getGeneralWeatherDescriptionId(Fragment ui, int weatherCode, double cloudCover) {
        int groupCode = getWeatherGroupCode(weatherCode);
        if (groupCode == NO_PRECIP_GROUP_CODE) {
            // When there's no precipitation, we report the cloud cover
            return getCloudinessStringId(cloudCover);
        } else {
            return getResourceId(ui, getWeatherCodeGroupLookupString(weatherCode));
        }
    }

    public static int getWeatherSummaryId(Fragment ui, int weatherCode) {
        int groupCode = getWeatherGroupCode(weatherCode);
        if (groupCode == NO_PRECIP_GROUP_CODE) {
            // When there's no precipitation, we report the cloudiness (which is the weather code)
            return getResourceId(ui, getWeatherCodeLookupString(weatherCode));
        } else {
            // When there is precipitation, we report the precipitation type (the group)
            return getResourceId(ui, getWeatherCodeGroupLookupString(weatherCode));
        }
    }

    public static String getPopString(Fragment ui, double pop) {
        String popString = ui.getResources().getString(R.string.pop);
        return String.format("%s: %s%%", popString, roundedDecimal.format(pop*100.0));
    }

    public static String getWeatherDetailsString(Fragment ui, int weatherCode, double pop) {
        int groupCode = getWeatherGroupCode(weatherCode);
        if (groupCode == NO_PRECIP_GROUP_CODE) {
            // When there's no precipitation, we report the precipitation chance
            return getPopString(ui, pop);
        } else {
            // When there is precipitation, we report the precipitation details (the weather code)
            int id = getResourceId(ui, getWeatherCodeLookupString(weatherCode));
            return ui.getResources().getString(id);
        }
    }


    public static int getCloudinessStringId(double cloudCover) {
        /*
         * One standard protocol is to divide the sky into 8 parts and then count how many of them
         * are obscured by clouds. Different numbers of obscured sky parts give different text
         * descriptions.
         *
         * We have a percentage cloud cover. To approximate the above, we divide the 0%-100% scale
         * into ninths, and ask "In which part of our scale is our cloud cover percentage?".
         */
        cloudCover /= 100.0;
        if (cloudCover >= 8.0 / 9.0) {
            return R.string.clouds_top_eighth;
        } else if (cloudCover >= 6.0 / 9.0) {
            return R.string.clouds_6_to_7_eighths;
        } else if (cloudCover > 3.0 / 9.0) {
            return R.string.clouds_3_to_5_eighths;
        } else if (cloudCover > 1.0 / 9.0) {
            return R.string.clouds_1_to_2_eighths;
        } else {
            return R.string.clouds_bottom_eighth;
        }
    }
}
