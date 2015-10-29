package me.jtalk.android.geotasks.source;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CalendarsSource {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarsSource.class);

	private Context context;

	/**
	 * Creates new calendar in Android Calendar provider.
	 *
	 * @return id of created calendar
	 */
	public long addCalendar() {
		ContentValues values = new ContentValues();
		values.put(CalendarContract.Calendars.NAME, "GeoTasks calendar");
		values.put(CalendarContract.Calendars.VISIBLE, true);

		Uri uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, Boolean.TRUE.toString())
				.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "GeoTasks account")
				.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();

		Uri inserted = context.getContentResolver().insert(uri, values);

		long id = Long.valueOf(inserted.getLastPathSegment());

		LOG.info("Calendar with id {} created", id);
		
		return id;
	}
}
