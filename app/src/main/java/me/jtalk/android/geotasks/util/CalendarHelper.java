package me.jtalk.android.geotasks.util;

import static me.jtalk.android.geotasks.util.Assert.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CalendarHelper {

	private static final String SELECTION_ARGUMENT_APPENDER = "= ?";
	private static final String SELECTION_STRING_SEPARATOR = SELECTION_ARGUMENT_APPENDER + ") AND (";
	private static final int ESTIMATED_CALENDAR_FIELD_SIZE = 20;
	private static final int ESTIMATED_SELECTION_ITEM_SIZE = ESTIMATED_CALENDAR_FIELD_SIZE
			+ SELECTION_STRING_SEPARATOR.length();

	public static String buildSelection(String ...fields) {

		verifyArgument(fields.length > 0, "Fields must be non-empty");

		StringBuilder builder = new StringBuilder(fields.length * ESTIMATED_SELECTION_ITEM_SIZE);
		builder.append("((");
		Joiner.joinIn(builder, fields, SELECTION_STRING_SEPARATOR)
				.append(SELECTION_ARGUMENT_APPENDER)
				.append("))");
	}
}
