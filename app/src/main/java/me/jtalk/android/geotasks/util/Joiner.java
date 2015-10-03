package me.jtalk.android.geotasks.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Joiner {

	public static StringBuilder joinIn(StringBuilder builder, String[] items, String separator) {
		boolean first = true;
		for (String item : items) {
			if (!first) {
				builder.append(separator);
			} else {
				first = false;
			}
			builder.append(item);
		}
		return builder;
	}
}
