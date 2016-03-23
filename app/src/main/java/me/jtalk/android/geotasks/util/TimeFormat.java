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
import android.text.format.DateFormat;

import java.text.ParseException;
import java.util.Calendar;

public class TimeFormat {
	public static java.text.DateFormat getTimeFormat(Context context) {
		return DateFormat.getTimeFormat(context);
	}

	public static java.text.DateFormat getDateFormat(Context context) {
		return DateFormat.getDateFormat(context);
	}

	public static String formatTime(Context context, Calendar calendar) {
		return innerFormat(getTimeFormat(context), calendar);
	}

	public static String formatDate(Context context, Calendar calendar) {
		return innerFormat(getDateFormat(context), calendar);
	}

	public static String formatDateTime(Context context, Calendar calendar) {
		return innerFormat(java.text.DateFormat.getDateTimeInstance(
						java.text.DateFormat.SHORT,
						java.text.DateFormat.SHORT,
						context.getResources().getConfiguration().locale),
				calendar);
	}

	public static Calendar parseTime(Context context, String timeText) throws ParseException {
		return innerParse(DateFormat.getTimeFormat(context), timeText);
	}

	public static Calendar parseDate(Context context, String dateText) throws ParseException {
		return innerParse(DateFormat.getDateFormat(context), dateText);
	}

	public static Calendar parseDateTime(Context context, String dateTimeText) throws ParseException {
		return innerParse(java.text.DateFormat.getDateTimeInstance(
						java.text.DateFormat.SHORT,
						java.text.DateFormat.SHORT,
						context.getResources().getConfiguration().locale),
				dateTimeText);
	}

	private static Calendar innerParse(java.text.DateFormat format, String timeText) throws ParseException {
		if (timeText.isEmpty()) {
			return null;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(format.parse(timeText));
		return calendar;
	}

	private static String innerFormat(java.text.DateFormat format, Calendar calendar) {
		return (calendar != null) ? format.format(calendar.getTime()) : "";
	}
}
