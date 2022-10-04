package su.thepeople.weather;

import androidx.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Helper class to handle the boilerplate involved in constructing a URL query
 */
public class URLBuilder {

    private static class NVPair {
        public String name;
        public String value;
        public NVPair(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private String address;
    private final List<NVPair> params;

    private URLBuilder() {
        address = "";
        params = new ArrayList<>();
    }

    @NonNull
    public static URLBuilder url() {
        return new URLBuilder();
    }

    public URLBuilder withHttpsAddress(String address) {
        this.address = address;
        return this;
    }

    public URLBuilder withParam(String name, String value) {
        params.add(new NVPair(name, value));
        return this;
    }

    private static String encode(String raw) {
        try {
            // URLEncoder requires you to pass in an encoding, and also requires that encoding to be "UTF-8".
            return URLEncoder.encode(raw, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private static String getSingleParamString(@NonNull NVPair param) {
        String quotedName = encode(param.name);
        String quotedValue = encode(param.value);
        return String.format("%s=%s", quotedName, quotedValue);
    }

    @NonNull
    private String getFullParamString() {
        StringJoiner joiner = new StringJoiner("&");
        for (NVPair param : params) {
            joiner.add(getSingleParamString(param));
        }
        return joiner.toString();
    }

    public URL build() {
        String urlStr = params.isEmpty() ?
                address :
                String.format("%s?%s", address, getFullParamString());
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            // Programming error if we ever get here
            throw new RuntimeException(e);
        }
    }
}
