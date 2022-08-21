package su.thepeople.weather.ui.main;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import su.thepeople.weather.R;

public class Utils {

    private static final DecimalFormat roundedDecimal = new DecimalFormat("###");

    public static String getTemperatureString(double temperature) {
        return String.format("%s°", roundedDecimal.format(temperature));
    }

    private static final DateTimeFormatter hourOnlyFormatter = DateTimeFormatter.ofPattern("h a");

    public static String getHourString(LocalDateTime dt) {
        return dt.format(hourOnlyFormatter);
    }

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
