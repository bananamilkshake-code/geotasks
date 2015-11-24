package me.jtalk.android.geotasks.util;

import android.database.Cursor;

import static me.jtalk.android.geotasks.util.Assert.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CursorHelper {

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
	 * Wrapper method to get long field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field field to retrieve value from
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
	 * @param field field to retrieve value from
	 * @return value of the field in cursor
	 * @see Cursor#getString
	 */
	public static String getString(Cursor cursor, String field) {
		return cursor.getString(cursor.getColumnIndex(field));
	}

	/**
	 * Wrapper method to get double field value from cursor.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field field to retrieve value from
	 * @return value of the field in cursor
	 * @see Cursor#getDouble
	 */
	public static double getDouble(Cursor cursor, String field) {
		return cursor.getDouble(cursor.getColumnIndex(field));
	}

	/**
	 * Wrapper method to check if column value in cursor is null.
	 *
	 * @param cursor cursor to retrieve value from
	 * @param field field to retrieve value from
	 * @return true if column field contains NULL, false otherwise
	 */
	public static boolean isNull(Cursor cursor, String field) {
		return cursor.getType(cursor.getColumnIndex(field)) == Cursor.FIELD_TYPE_NULL;
	}
}
