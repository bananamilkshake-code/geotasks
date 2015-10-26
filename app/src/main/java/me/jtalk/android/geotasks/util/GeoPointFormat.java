package me.jtalk.android.geotasks.util;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.text.MessageFormat;

public class GeoPointFormat {
	private static final String FORMAT_GEOPINT = "lon:{0} lat:{1}";
	private static final String FORMAT_GEOPINT_SIMPLE = "{0} {1}";

	public static String format(IGeoPoint geoPoint) {
		return MessageFormat.format(FORMAT_GEOPINT, geoPoint.getLongitude(), geoPoint.getLatitude());
	}

	public static String formatSimple(IGeoPoint geoPoint) {
		return MessageFormat.format(FORMAT_GEOPINT_SIMPLE, geoPoint.getLongitude(), geoPoint.getLatitude());
	}

	public static IGeoPoint parse(String str) {
		String[] values = str.split(" ");

		double lon = Double.valueOf(values[0]);
		double lat = Double.valueOf(values[1]);

		return new GeoPoint(lat, lon);
	}
}
