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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assert {

	public static void verifyArgument(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void verifyArgument(boolean expression, String format, Object... args) {
		if (!expression) {
			String message = MessageFormat.format(format, args);
			throw new IllegalArgumentException(message);
		}
	}

	public static void verifyState(boolean expression, String message) {
		if (!expression) {
			throw new IllegalStateException(message);
		}
	}

	public static void verifyState(boolean expression, String format, Object... args) {
		if (!expression) {
			String message = MessageFormat.format(format, args);
			throw new IllegalStateException(message);
		}
	}

	public static <T> void verifyNotNull(T object, String format, Object ...args) {
		if (object == null) {
			String message = MessageFormat.format(format, args);
			throw new NullPointerException(message);
		}
	}

	public static <T> void verifyNotNull(T object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
	}
}
