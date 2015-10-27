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
import android.util.Log;
import android.widget.CursorAdapter;

import java.text.MessageFormat;
import java.util.TimeZone;

import me.jtalk.android.geotasks.util.CalendarHelper;

public class EventsSource implements LoaderManager.LoaderCallbacks<Cursor>, EventsInserter {
	public static final String TAG = EventsSource.class.getName();

	public static final long DEFAULT_CALENDAR = -1;

	public static final long DEFAULT_START_TIME = -1;
	public static final long DEFAULT_END_TIME = -1;

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

	public EventsSource(Context context, CursorAdapter eventsAdapter, long calendarId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
				throw new SecurityException("No permission to write calendar");
			}
		}

		this.context = context;
		this.eventsAdapter = eventsAdapter;
		this.calendarId = calendarId;

		Log.d(TAG, MessageFormat.format("EventSource {0} is created for context {1}", this, context));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, MessageFormat.format("EventsSource created loader for {0} calendar", calendarId));

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

	@Override
	public void addEvent(String title, String description, String location, long startTime, long endTime) throws SecurityException {
		Log.d(TAG, MessageFormat.format("Inserting new event for calendarId {0}", calendarId));

		ContentValues values = new ContentValues();
		values.put(Events.CALENDAR_ID, calendarId);
		values.put(Events.TITLE, title);
		values.put(Events.DESCRIPTION, description);
		values.put(Events.EVENT_LOCATION, location);
		values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

		values.put(Events.DTSTART, startTime);
		values.put(Events.DTEND, endTime);

		Uri created = this.context.getContentResolver().insert(Events.CONTENT_URI, values);
		Log.d(TAG, MessageFormat.format("New event was created. Uri: {0}", created.toString()));
	}

	public void removeEvent(long eventId) {
		Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventId);
		this.context.getContentResolver().delete(deleteUri, null, null);
	}
}
