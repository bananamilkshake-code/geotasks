package me.jtalk.android.geotasks.util;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.text.MessageFormat;

public class GeoPointFormat {
	private static final String FORMAT_GEOPOINT = "lat: {0} lon: {1}";
	private static final String FORMAT_GEOPOINT_SIMPLE = "{0} {1}";

	public static String format(IGeoPoint geoPoint) {
		return MessageFormat.format(FORMAT_GEOPOINT, geoPoint.getLatitude(), geoPoint.getLongitude());
	}

	public static String formatSimple(IGeoPoint geoPoint) {
		return MessageFormat.format(FORMAT_GEOPOINT_SIMPLE, geoPoint.getLatitude(), geoPoint.getLongitude());
	}

	public static IGeoPoint parse(String str) {
		String[] values = str.split(" ");

		double lat = Double.valueOf(values[2]);
		double lon = Double.valueOf(values[1]);

		return new GeoPoint(lat, lon);
	}
}
