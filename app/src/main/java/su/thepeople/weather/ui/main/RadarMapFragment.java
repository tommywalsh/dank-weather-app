package su.thepeople.weather.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;

import java.net.URL;
import java.util.Locale;

import su.thepeople.weather.R;
import su.thepeople.weather.SimpleLocation;
import su.thepeople.weather.URLBuilder;
import su.thepeople.weather.WeatherData;
import su.thepeople.weather.WeatherReport;

public class RadarMapFragment extends Fragment {

    private WebView webView;

    public RadarMapFragment() {
        // Required empty public constructor
    }

    private URL getRadarUrl(SimpleLocation location) {
        int zoomLevel = 7;
        // Rainviewer expects US-formatted numbers (regardless of whatever locale user has set)
        String mapSpec = String.format(Locale.US, "%f,%f,%d", location.lat, location.lng, zoomLevel);
        return URLBuilder
                .url()
                .withHttpsAddress("https://www.rainviewer.com/map.html")
                .withParam("loc", mapSpec)
                .withParam("layer", "radar")
                .withParam("oFa", "1") // Show animations in "fast" mode
                .withParam("oC", "1")  // Distinguish between areas covered and not covered by available radar
                .withParam("oU", "0")  // Do not show time in UTC (use local time instead)
                .withParam("oCs", "0") // Do not show legend
                .withParam("oAP", "1") // Autoplay animation
                .withParam("c", "6")   // "Rainbow" color scheme (best contrast between rain and snow)
                .withParam("o", "60")  // % opacity of radar overlay
                .withParam("sm", "0")  // Do not smooth data
                .withParam("sn", "1")  // Show snow separately from rain
                .build();
    }

    private void onNewReport(WeatherReport report) {
        webView.loadUrl(getRadarUrl(report.location).toString());
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_radar_map, container, false);

        // As of this writing (2022/10/04), these setting allow the Rainviewer map to display correctly.
        webView = inflatedView.findViewById(R.id.webView);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);

        WeatherData.latestReport().observe(getViewLifecycleOwner(), this::onNewReport);

        return inflatedView;
    }
}
