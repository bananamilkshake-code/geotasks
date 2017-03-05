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

import android.support.annotation.NonNull;
import android.util.Log;

import org.acra.ACRA;

import java.text.MessageFormat;

/**
 * Logging with message formatting.
 * Formatting is implemented via {@MessageFormat} class.
 */
public class Logger {
	private final String tag;

	public Logger(Class clazz) {
		this(clazz.getName());
	}

	public Logger(@NonNull String tag) {
		this.tag = tag;
	}

	public void verbose(String format, Object... objects) {
		Log.v(tag, format(format, objects));
	}

	public void debug(String format, Object... objects) {
		Log.d(tag, format(format, objects));
	}

	public void info(String format, Object... objects) {
		Log.i(tag, format(format, objects));
	}

	public void warn(String format, Object... objects) {
		Log.w(tag, format(format, objects));
	}

	public void warn(Throwable throwable, String format, Object... objects) {
		ACRA.getErrorReporter().handleSilentException(throwable);
		Log.w(tag, format(format, objects), throwable);
	}

	public void error(String format, Object... objects) {
		Log.e(tag, format(format, objects));
	}

	public void error(Throwable throwable, String format, Object... objects) {
		ACRA.getErrorReporter().handleSilentException(throwable);
		Log.e(tag, format(format, objects), throwable);
	}

	public void fatal(String format, Object... objects) {
		Log.wtf(tag, format(format, objects));
	}

	public void fatal(Throwable throwable, String format, Object... objects) {
		ACRA.getErrorReporter().handleSilentException(throwable);
		Log.wtf(tag, format(format, objects), throwable);
	}

	private String format(String format, Object... objects) {
		return MessageFormat.format(format, objects);
	}
}
