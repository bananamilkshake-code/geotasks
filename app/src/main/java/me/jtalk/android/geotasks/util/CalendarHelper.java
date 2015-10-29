package me.jtalk.android.geotasks.util;

import android.database.Cursor;

import java.text.DateFormat;
import java.util.Calendar;

import static me.jtalk.android.geotasks.util.Assert.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.jtalk.android.geotasks.source.EventsSource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CalendarHelper {

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
	 * Wrapper method to get field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field field to retrieve value from
	 * @return value of the field in cursor
	 * @see Cursor.getLong()
	 */
	public static Long getLong(Cursor cursor, String field) {
		return cursor.getLong(cursor.getColumnIndex(field));
	}

	public static String getString(Cursor cursor, String field) {
		return cursor.getString(cursor.getColumnIndex(field));
	}
}
