package me.jtalk.android.geotasks.source;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;

import lombok.AllArgsConstructor;
import me.jtalk.android.geotasks.util.Logger;

@AllArgsConstructor
public class CalendarsSource {
	private static final Logger LOG = new Logger(CalendarsSource.class);

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

		LOG.info("Calendar with id {0} created", id);

		return id;
	}
}
