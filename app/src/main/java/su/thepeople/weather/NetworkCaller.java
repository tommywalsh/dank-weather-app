package su.thepeople.weather;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

public class NetworkCaller {

    private static String toString(InputStream stream) {
        InputStreamReader isReader = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(isReader);
        return reader.lines().collect(Collectors.joining("\n"));
    }

    private static String getStringResult(URL url) throws IOException {
        /*
         * Our only current use case for this class is to construct JSON objects from the
         * network connection's stream. The JSON library we're using does not have an API to deal
         * with streams directly, only String objects. So, for now, we always convert all network
         * streams to String objects.  If we ever have a use case where we can use the stream
         * directly, we'll want to break apart the connection/fetch and the String conversion.
         */
        URLConnection connection = url.openConnection();
        // TODO: What if we get a server-reported error?
        InputStream stream = connection.getInputStream();
        return toString(stream);
    }

    public static JSONObject getJSONObjectResult(URL url) throws IOException, JSONException {
        return new JSONObject(getStringResult(url));
    }
}
