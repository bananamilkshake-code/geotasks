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
	 * @param groupCursor
	 * @return
	 */
	public static Cursor clone(Cursor groupCursor) {
		return clone(groupCursor, groupCursor.getCount());
	}

	/**
	 * Creates new cursor which data is copied from first {@amount}
	 * elements of group cursor (or less if there is not enough rows).
	 *
	 * @param groupCursor
	 * @param amount
	 * @return
	 */
	public static Cursor clone(Cursor groupCursor, int amount) {
		final String[] columns = groupCursor.getColumnNames();
		MatrixCursor cursor = new MatrixCursor(columns, amount);

		int startPosition = groupCursor.getPosition();

		for (int i = 0; i < amount && !groupCursor.isAfterLast(); ++i) {
			MatrixCursor.RowBuilder rowBuilder = cursor.newRow();
			for (String column : columns) {
				switch (getType(groupCursor, column)) {
					case Cursor.FIELD_TYPE_NULL:
						rowBuilder.add(column, null);
						break;
					case Cursor.FIELD_TYPE_INTEGER:
						rowBuilder.add(column, getLong(groupCursor, column));
						break;
					case Cursor.FIELD_TYPE_FLOAT:
						rowBuilder.add(column, getFloat(groupCursor, column));
						break;
					case Cursor.FIELD_TYPE_STRING:
						rowBuilder.add(column, getString(groupCursor, column));
						break;
					case Cursor.FIELD_TYPE_BLOB:
						rowBuilder.add(column, getBlob(groupCursor, column));
						break;
				}
			}

			groupCursor.moveToNext();
		}

		// Restore position.
		groupCursor.moveToPosition(startPosition);

		return cursor;
	}
}
