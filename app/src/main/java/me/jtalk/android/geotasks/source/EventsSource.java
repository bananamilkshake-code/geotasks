package me.jtalk.android.geotasks.source;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.widget.CursorAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import me.jtalk.android.geotasks.util.CalendarHelper;

public class EventsSource implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final Logger LOG = LoggerFactory.getLogger(EventsSource.class);

	public static final long DEFAULT_CALENDAR = -1;
	public static final long DEFAULT_TIME_VALUE = -1;

	public static final Calendar EMPTY_TIME = null;

	private Context context;
	private CursorAdapter eventsAdapter;

	private long calendarId;

	private static final String[] PROJECTION_EVENTS = new String[]{
			Events._ID,
			Events.CALENDAR_ID,
			Events.TITLE,
			Events.DESCRIPTION,
			Events.EVENT_LOCATION,
			Events.DTSTART
	};

	public static Event extractEvent(Cursor cursor) {
		return new Event(
				CalendarHelper.getString(cursor, Events.TITLE),
				getTimeText(cursor, Events.DTSTART));
	}

	/**
	 * Retrieves column value from cursor and converts it to calendar.
	 * If start time value is not been set for that event null will be returned.
	 *
	 * @param cursor
	 * @param timeField field name that contains time value (long)
	 * @return calendar with start time value or null if time hadn't been set
	 */
	public static String getTimeText(Cursor cursor, String timeField) {
		long timeInMillis = CalendarHelper.getLong(cursor, timeField);
		if (timeInMillis == DEFAULT_TIME_VALUE) {
			return null;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		return DateFormat.getDateTimeInstance().format(calendar.getTime());
	}

	public EventsSource(Context context, CursorAdapter eventsAdapter, long calendarId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
				throw new SecurityException("No permission to write calendar");
			}
		}

		this.context = context;
		this.eventsAdapter = eventsAdapter;
		this.calendarId = calendarId;

		LOG.debug("EventSource {} is created for context {}", this, context);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		LOG.debug("EventsSource created loader for {} calendar", calendarId);

		String selection = CalendarHelper.buildProjection(Events.CALENDAR_ID);
		String[] selectionArgs = new String[]{String.valueOf(calendarId)};

		return new CursorLoader(context, Events.CONTENT_URI,
				PROJECTION_EVENTS, selection, selectionArgs, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		eventsAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		eventsAdapter.swapCursor(null);
	}

	/**
	 * Creates new event in Android Calendar Provider.
	 *
	 * @param title
	 * @param description
	 * @param location
	 * @param startTime event start time. This value can be null if start time is not set.
	 * @param endTime event end time. This value can be null if start time is not set.
	 * @throws SecurityException is thrown if Calendar permission is not granted for application.
	 */
	public void addEvent(String title, String description, String location, Calendar startTime, Calendar endTime) throws SecurityException {
		LOG.debug("Inserting new event for calendarId {}", calendarId);

		ContentValues values = new ContentValues();
		values.put(Events.CALENDAR_ID, calendarId);
		values.put(Events.TITLE, title);
		values.put(Events.DESCRIPTION, description);
		values.put(Events.EVENT_LOCATION, location);
		values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

		values.put(Events.DTSTART, getMillis(startTime));
		values.put(Events.DTEND, getMillis(endTime));

		Uri created = this.context.getContentResolver().insert(Events.CONTENT_URI, values);
		LOG.debug("New event was created. Uri: {}", created.toString());
	}

	/**
	 * Remove event with {@id} from Calendars Provider.
	 *
	 * @param id event id to remove
	 */
	public void removeEvent(long id) {
		Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
		this.context.getContentResolver().delete(deleteUri, null, null);
	}

	/**
	 * Safety retrieves millis from calendar: null calendar means that no time is set
	 * and {@DEFAULT_TIME_VALUE} must be returned.
	 *
	 * @param time
	 * @return time in milliseconds or @{DEFAULT_TIME_VALUE} if time is null
	 */
	private static long getMillis(Calendar time) {
		return (time != EMPTY_TIME) ? time.getTimeInMillis() : DEFAULT_TIME_VALUE;
	}
}
