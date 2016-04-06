/*
 * Copyright (C) 2016 Liza Lukicheva
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
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
}
