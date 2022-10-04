package su.thepeople.weather.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import su.thepeople.weather.R;

@SuppressWarnings("deprecation")
public class WeatherPagerAdapter extends FragmentPagerAdapter {

    private static class TabDefinition {
        @StringRes public int titleStringId;
        public Supplier<Fragment> fragmentCreator;
        public TabDefinition(@StringRes int titleStringId, Supplier<Fragment> fragmentCreator) {
            this.titleStringId = titleStringId;
            this.fragmentCreator = fragmentCreator;
        }
    }

    private static final List<TabDefinition> tabDefinitions = new ArrayList<>();

    private final Context context;

    public WeatherPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;

        tabDefinitions.add(new TabDefinition(R.string.tab_text_now, CurrentWeatherFragment::new));
        tabDefinitions.add(new TabDefinition(R.string.tab_text_today, HourlyWeatherFragment::new));
        tabDefinitions.add(new TabDefinition(R.string.tab_text_future, DailyWeatherFragment::new));
        tabDefinitions.add(new TabDefinition(R.string.tab_text_radar, RadarMapFragment::new));
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        assert position <= tabDefinitions.size();
        return tabDefinitions.get(position).fragmentCreator.get();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        assert position <= tabDefinitions.size();
        return context.getResources().getString(tabDefinitions.get(position).titleStringId);
    }

    @Override
    public int getCount() {
        return tabDefinitions.size();
    }
}