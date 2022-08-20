package su.thepeople.weather.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.LiveData;

import su.thepeople.weather.R;
import su.thepeople.weather.WeatherReport;

@SuppressWarnings("deprecation")
public class WeatherPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_now, R.string.tab_text_today, R.string.tab_text_future};
    private final Context mContext;
    private final LiveData<WeatherReport> weatherReport;

    public WeatherPagerAdapter(Context context, FragmentManager fm, LiveData<WeatherReport> weatherReport) {
        super(fm);
        mContext = context;
        this.weatherReport = weatherReport;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = CurrentWeatherFragment.newInstance();
        /*} else if (position == 1) {
            fragment = HourlyWeatherFragment.newInstance(1);*/
        } else {
            fragment = PlaceholderFragment.newInstance(weatherReport);
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }

}