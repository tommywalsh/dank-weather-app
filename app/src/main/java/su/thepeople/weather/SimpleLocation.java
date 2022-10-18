package su.thepeople.weather;

import android.location.Location;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

public class SimpleLocation {
    public double lat;
    public double lng;
    public LocationType type;
    public LocalDateTime timestamp;
    public String name;

    public enum LocationType {
        OUTDATED,
        LAST_KNOWN,
        CURRENT
    }

    private static LocalDateTime asLocalDateTime(long utcMillisecondsSinceEpoch) {
        String zoneId = TimeZone.getDefault().getID();
        return Instant.ofEpochMilli(utcMillisecondsSinceEpoch).atZone(ZoneId.of(zoneId)).toLocalDateTime();
    }

    public SimpleLocation(Location l, LocationType type, String name) {
        this.lat = l.getLatitude();
        this.lng = l.getLongitude();
        this.timestamp = asLocalDateTime(l.getTime());
        this.type = type;
        this.name = name;
    }
}
