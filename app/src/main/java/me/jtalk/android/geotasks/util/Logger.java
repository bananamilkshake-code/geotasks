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

	private String format(String format, Object... objects) {
		return MessageFormat.format(format, objects);
	}
}
