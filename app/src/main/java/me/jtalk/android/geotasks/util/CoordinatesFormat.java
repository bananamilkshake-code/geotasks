package me.jtalk.android.geotasks.util;

import java.text.MessageFormat;
import java.text.NumberFormat;

import me.jtalk.android.geotasks.location.TaskCoordinates;

public class CoordinatesFormat {
	private static final String SPLITTER = " ";

	private static final String FORMAT_COORDINATES = "lat: {0}; lon: {1}";
	private static final String FORMAT_COORDINATES_SIMPLE = "{0}" + SPLITTER + "{1}";

	private static final int INTEGER_DIGITS_COUNT = 3;
	private static final int FRACTION_DIGITS_COUNT = 6;
	private static final NumberFormat GEO_FORMAT;

	/**
	 * Specifies length of coordinates formatted string.
	 */
	public static final int POINT_ACCURACY = INTEGER_DIGITS_COUNT + SPLITTER.length() + FRACTION_DIGITS_COUNT;

	static {
		GEO_FORMAT = NumberFormat.getInstance();
		GEO_FORMAT.setMinimumIntegerDigits(INTEGER_DIGITS_COUNT);
		GEO_FORMAT.setMaximumFractionDigits(INTEGER_DIGITS_COUNT);
		GEO_FORMAT.setMaximumFractionDigits(FRACTION_DIGITS_COUNT);
		GEO_FORMAT.setMinimumFractionDigits(FRACTION_DIGITS_COUNT);
	}

	public static String format(TaskCoordinates taskCoordinates) {
		return MessageFormat.format(FORMAT_COORDINATES, taskCoordinates.getLatitude(), taskCoordinates.getLongitude());
	}

	public static String formatSimple(TaskCoordinates taskCoordinates) {
		return MessageFormat.format(FORMAT_COORDINATES_SIMPLE,
				GEO_FORMAT.format(taskCoordinates.getLatitude()),
				GEO_FORMAT.format(taskCoordinates.getLongitude()));
	}

	/**
	 * Parses geo point values (lat and lon) from simple formatted string.
	 *
	 * @param str
	 * @return
	 */
	public static TaskCoordinates parse(String str) {
		String[] values = str.split(SPLITTER);

		double lat = Double.valueOf(values[2]);
		double lon = Double.valueOf(values[1]);

		return new TaskCoordinates(lat, lon);
	}
}
