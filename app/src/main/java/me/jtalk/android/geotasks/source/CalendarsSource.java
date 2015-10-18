package me.jtalk.android.geotasks.source;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.MessageFormat;

public class CalendarsSource {
	private static final String TAG = CalendarsSource.class.getName();

	private Context context;

	public CalendarsSource(Context context) {
		this.context = context;
	}

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

		Log.d(TAG, MessageFormat.format("Calendar with id {0} created", id));
		
		return id;
	}
}
