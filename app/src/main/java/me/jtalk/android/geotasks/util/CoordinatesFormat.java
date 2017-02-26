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

import android.content.Context;

import com.google.common.base.Splitter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.location.TaskCoordinates;

import static java.text.MessageFormat.format;
import static me.jtalk.android.geotasks.util.Assert.verifyArgument;

public class CoordinatesFormat {

	private static final String DB_SPLITTER = " ";
	private static final String DB_FORMAT = "{0}" + DB_SPLITTER + "{1}";

	private static final int INTEGRAL_DIGITS_COUNT = 3;
	private static final int FRACTION_DIGITS_COUNT = 6;

	private final Context context;
	private final NumberFormat uiFormat;
	private final NumberFormat databaseFormat;
	private final Locale locale;

	protected CoordinatesFormat(Context context) {
		this.context = context;
		this.locale = context.getResources().getConfiguration().locale;
		this.uiFormat = buildGeoNumberFormat(locale);
		this.databaseFormat = buildGeoNumberFormat(Locale.UK);
	}

	public static CoordinatesFormat getInstance(Context context) {
		return new CoordinatesFormat(context);
	}

	public String prettyFormatShort(TaskCoordinates taskCoordinates) {
		return prettyFormatByType(taskCoordinates, context.getString(R.string.location_pick_coordinates_format_short));
	}

	public String prettyFormatLong(TaskCoordinates taskCoordinates) {
		return prettyFormatByType(taskCoordinates, context.getString(R.string.location_pick_coordinates_format_long));
	}

	public String formatSingleCoordinate(double coordinate) {
		return uiFormat.format(coordinate);
	}

	public double parseSingleCoordinate(String coordinate) throws ParseException {
		return uiFormat.parse(coordinate)
				.doubleValue();
	}

	public String formatForDatabase(TaskCoordinates taskCoordinates) {
		if (taskCoordinates == null) {
			return null;
		}
		return format(DB_FORMAT,
				databaseFormat.format(taskCoordinates.getLatitude()),
				databaseFormat.format(taskCoordinates.getLongitude()));
	}

	public TaskCoordinates parseFromDatabase(String coordinates) throws ParseException {
		List<String> data = Splitter.on(DB_SPLITTER).limit(2).trimResults().splitToList(coordinates);
		verifyArgument(data.size() == 2, "The supplied string does not represent a valid database location value: \"{0}\"; expected format is {1}", coordinates, DB_FORMAT);
		return new TaskCoordinates(
				databaseFormat.parse(data.get(0)).doubleValue(),
				databaseFormat.parse(data.get(1)).doubleValue());
	}

	private static NumberFormat buildGeoNumberFormat(Locale locale) {
		NumberFormat result = NumberFormat.getInstance(locale);
		result.setMinimumIntegerDigits(INTEGRAL_DIGITS_COUNT);
		result.setMaximumFractionDigits(INTEGRAL_DIGITS_COUNT);
		result.setMaximumFractionDigits(FRACTION_DIGITS_COUNT);
		result.setMinimumFractionDigits(FRACTION_DIGITS_COUNT);
		return result;
	}

	private String prettyFormatByType(TaskCoordinates taskCoordinates, String string) {
		if (taskCoordinates == null) {
			return "";
		}
		return format(string, taskCoordinates.getLatitude(), taskCoordinates.getLongitude());
	}
}
