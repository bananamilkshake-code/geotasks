package me.jtalk.android.geotasks.util;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.text.MessageFormat;
import java.text.NumberFormat;

public class GeoPointFormat {
	private static final String SPLITTER = " ";

	private static final String FORMAT_GEOPOINT = "lat: {0}; lon: {1}";
	private static final String FORMAT_GEOPOINT_SIMPLE = "{0}" + SPLITTER + "{1}";

	private static final int INTEGER_DIGITS_COUNT = 3;
	private static final int FRACTION_DIGITS_COUNT = 6;
	private static final NumberFormat GEO_FORMAT;

	/**
	 * Specifies length of geopoint formatted string.
	 */
	public static final int POINT_ACCURACY = INTEGER_DIGITS_COUNT + SPLITTER.length() + FRACTION_DIGITS_COUNT;

	static {
		GEO_FORMAT = NumberFormat.getInstance();
		GEO_FORMAT.setMinimumIntegerDigits(INTEGER_DIGITS_COUNT);
		GEO_FORMAT.setMaximumFractionDigits(INTEGER_DIGITS_COUNT);
		GEO_FORMAT.setMaximumFractionDigits(FRACTION_DIGITS_COUNT);
		GEO_FORMAT.setMinimumFractionDigits(FRACTION_DIGITS_COUNT);
	}

	public static String format(IGeoPoint geoPoint) {
		return MessageFormat.format(FORMAT_GEOPOINT, geoPoint.getLatitude(), geoPoint.getLongitude());
	}

	public static String formatSimple(IGeoPoint geoPoint) {
		return MessageFormat.format(FORMAT_GEOPOINT_SIMPLE,
				GEO_FORMAT.format(geoPoint.getLatitude()),
				GEO_FORMAT.format(geoPoint.getLongitude()));
	}

	/**
	 * Parses geo point values (lat and lon) from simple formatted string.
	 *
	 * @param str
	 * @return
	 */
	public static IGeoPoint parse(String str) {
		String[] values = str.split(SPLITTER);

		double lat = Double.valueOf(values[2]);
		double lon = Double.valueOf(values[1]);

		return new GeoPoint(lat, lon);
	}
}
