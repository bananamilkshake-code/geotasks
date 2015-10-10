package me.jtalk.android.geotasks.util;

import java.text.MessageFormat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assert {

	public static void verifyArgument(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void verifyArgument(boolean expression, String format, Object ...args) {
		if (!expression) {
			String message = MessageFormat.format(format, args);
			throw new IllegalArgumentException(message);
		}
	}
}
