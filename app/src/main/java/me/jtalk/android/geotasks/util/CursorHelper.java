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

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.CalendarContract;

import java.util.Calendar;

import static me.jtalk.android.geotasks.util.Assert.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CursorHelper {

	public static final String EVENT_LATITUDE = "latitude";
	public static final String EVENT_LONGITUDE = "longitude";

	public static final long DEFAULT_TIME_VALUE = -1;

	private static final String SELECTION_ARGUMENT_APPENDER = "= ?";
	private static final String SELECTION_STRING_SEPARATOR = SELECTION_ARGUMENT_APPENDER + ") AND (";
	private static final int ESTIMATED_CALENDAR_FIELD_SIZE = 20;
	private static final int ESTIMATED_SELECTION_ITEM_SIZE = ESTIMATED_CALENDAR_FIELD_SIZE
			+ SELECTION_STRING_SEPARATOR.length();

	public static String buildProjection(String... fields) {

		verifyArgument(fields.length > 0, "Fields must be non-empty");

		StringBuilder builder = new StringBuilder(fields.length * ESTIMATED_SELECTION_ITEM_SIZE);
		builder.append("((");
		return Joiner.joinIn(builder, fields, SELECTION_STRING_SEPARATOR)
				.append(SELECTION_ARGUMENT_APPENDER)
				.append("))").toString();
	}

	/**
	 * Wrapper method to get boolean field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field  field to retrieve value from
	 * @return value of the field in cursor
	 */
	public static boolean getBoolean(Cursor cursor, String field) {
		return cursor.getInt(cursor.getColumnIndex(field)) != 0;
	}

	/**
	 * Wrapper method to get long field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field  field to retrieve value from
	 * @return value of the field in cursor
	 * @see Cursor#getLong
	 */
	public static long getLong(Cursor cursor, String field) {
		return cursor.getLong(cursor.getColumnIndex(field));
	}

	/**
	 * Wrapper method to get string field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field  field to retrieve value from
	 * @return value of the field in cursor
	 * @see Cursor#getString
	 */
	public static String getString(Cursor cursor, String field) {
		return cursor.getString(cursor.getColumnIndex(field));
	}

	/**
	 * Wrapper method to get int field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field  field to retrieve value from
	 * @return value of the field in cursor
	 * @see Cursor#getInt
	 */
	private static int getInt(Cursor cursor, String field) {
		return cursor.getInt(cursor.getColumnIndex(field));
	}

	/**
	 * Wrapper method to get float field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field  field to retrieve value from
	 * @return value of the field in cursor
	 * @see Cursor#getFloat
	 */
	public static Object getFloat(Cursor cursor, String field) {
		return cursor.getFloat(cursor.getColumnIndex(field));
	}

	/**
	 * Wrapper method to get double field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field  field to retrieve value from
	 * @return value of the field in cursor
	 * @see Cursor#getDouble
	 */
	public static double getDouble(Cursor cursor, String field) {
		return cursor.getDouble(cursor.getColumnIndex(field));
	}

	/**
	 * Wrapper method to get blob field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field  field to retrieve value from
	 * @return value of the field in cursor
	 * @see Cursor#getBlob
	 */
	public static Object getBlob(Cursor cursor, String field) {
		return cursor.getBlob(cursor.getColumnIndex(field));
	}

	/**
	 * Wrapper method to check if column value in cursor is null.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field  field to retrieve value from
	 * @return true if column field contains NULL, false otherwise
	 */
	public static boolean isNull(Cursor cursor, String field) {
		return getType(cursor, field) == Cursor.FIELD_TYPE_NULL;
	}

	/**
	 * Wrapper method to get field type from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field  field to retrieve type
	 * @return type of the field in cursor
	 * @see Cursor#getType
	 */
	public static int getType(Cursor cursor, String field) {
		return cursor.getType(cursor.getColumnIndex(field));
	}

	/**
	 * Creates new cursor which data is copied from group cursor.
	 *
	 * @param groupCursor cursor to be copied
	 * @return new cursor object that contains amount data from srcCursor
	 */
	public static Cursor clone(Cursor groupCursor) {
		return clone(groupCursor, groupCursor.getCount());
	}

	/**
	 * Creates new cursor which data is copied from first {@amount}
	 * elements of group cursor (or less if there is not enough rows).
	 *
	 * @param srcCursor cursor to be copied
	 * @param amount how much cursors must be copied
	 * @return new cursor object that contains amount data from srcCursor
	 */
	public static Cursor clone(Cursor srcCursor, int amount) {
		final String[] columns = srcCursor.getColumnNames();
		MatrixCursor cursor = new MatrixCursor(columns, amount);

		int startPosition = srcCursor.getPosition();

		for (int i = 0; i < amount && !srcCursor.isAfterLast(); ++i) {
			MatrixCursor.RowBuilder rowBuilder = cursor.newRow();
			for (String column : columns) {
				switch (getType(srcCursor, column)) {
					case Cursor.FIELD_TYPE_NULL:
						rowBuilder.add(column, null);
						break;
					case Cursor.FIELD_TYPE_INTEGER:
						rowBuilder.add(column, getLong(srcCursor, column));
						break;
					case Cursor.FIELD_TYPE_FLOAT:
						rowBuilder.add(column, getFloat(srcCursor, column));
						break;
					case Cursor.FIELD_TYPE_STRING:
						rowBuilder.add(column, getString(srcCursor, column));
						break;
					case Cursor.FIELD_TYPE_BLOB:
						rowBuilder.add(column, getBlob(srcCursor, column));
						break;
				}
			}

			srcCursor.moveToNext();
		}

		// Restore position.
		srcCursor.moveToPosition(startPosition);

		return cursor;
	}

	/**
	 * Extracts data for event from cursor (projection fields are {@link PROJECTION_EVENTS})
	 * and creates Event object from it.
	 *
	 * @param cursor cursor that stores event data
	 * @return Event object that was created from retrieved data.
	 */
	public static Event extractEvent(Cursor cursor) {
		long id = getLong(cursor, CalendarContract.Events._ID);
		String title = getString(cursor, CalendarContract.Events.TITLE);
		String description = getString(cursor, CalendarContract.Events.DESCRIPTION);
		Calendar startTime = extractTime(cursor, CalendarContract.Events.DTSTART, DEFAULT_TIME_VALUE);
		Calendar endTime = extractTime(cursor, CalendarContract.Events.DTEND, DEFAULT_TIME_VALUE);
		boolean hasAlarms = getBoolean(cursor, CalendarContract.Events.HAS_ALARM);
		TaskCoordinates geoPoint = extractCoordinates(cursor);
		return new Event(id, title, description, startTime, endTime, geoPoint, hasAlarms);
	}

	/**
	 * Retrieves location coordinates where event, which data is stored in current cursor,
	 * must occur. If no location coordinates had been set null will be returned.
	 *
	 * @param cursor cursor from which coordinates must be retrieved
	 * @return TaskCoordinates object that contains information about longitude and latitude.
	 * Returns null if location for event has not been set.
	 */
	private static TaskCoordinates extractCoordinates(Cursor cursor) {
		String locationValue = getString(cursor, CalendarContract.Events.EVENT_LOCATION);
		if (locationValue == null || locationValue.isEmpty()) {
			return null;
		}

		double lat = getDouble(cursor, EVENT_LATITUDE);
		double lon = getDouble(cursor, EVENT_LONGITUDE);
		return new TaskCoordinates(lat, lon);
	}

	/**
	 * Retrieves column value from cursor and converts it to calendar.
	 * If start time value is not been set for that event null will be returned.
	 *
	 * @param cursor cursor from which time must be retrieved
	 * @param timeField field name that contains time value (long)
	 * @return calendar with start time value or null if time hadn't been set
	 */
	public static Calendar extractTime(Cursor cursor, String timeField, long defaultValue) {
		long timeInMillis = getLong(cursor, timeField);
		if (timeInMillis == defaultValue) {
			return null;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		return calendar;
	}

	/**
	 * Safety retrieves millis from calendar: null calendar means that no time is set
	 * and {@DEFAULT_TIME_VALUE} must be returned.
	 *
	 * @param time Calendar object that contains time value. Can be null.
	 * @return time in milliseconds or @{DEFAULT_TIME_VALUE} if time is null
	 */
	public static long getMillis(Calendar time) {
		return (time != null) ? time.getTimeInMillis() : DEFAULT_TIME_VALUE;
	}
}
