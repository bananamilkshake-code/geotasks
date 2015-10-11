package me.jtalk.android.geotasks.source;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.widget.CursorAdapter;

import java.util.TimeZone;

import me.jtalk.android.geotasks.util.CalendarHelper;

public class EventsSource implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = EventsSource.class.getName();

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
		this.context = context;
		this.eventsAdapter = eventsAdapter;
		this.calendarId = calendarId;

		Log.d(TAG, String.format("EventSource %h is created for context %h", this, context));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, String.format("EventsSource created loader for %d calendar", calendarId));

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

	public void addEvent(String title, String description, long startTime) throws SecurityException {
		Log.d(TAG, String.format("Inserting new event for calendarId %d", calendarId));

		ContentValues values = new ContentValues();
		values.put(Events.CALENDAR_ID, calendarId);
		values.put(Events.TITLE, title);
		values.put(Events.DESCRIPTION, description);
		values.put(Events.DTSTART, startTime);
		values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

		values.put(Events.DTEND, -1);

		Uri created = this.context.getContentResolver().insert(Events.CONTENT_URI, values);
		Log.d(TAG, created.toString());
	}

	public void removeEvent(long eventId) {
		Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventId);
		this.context.getContentResolver().delete(deleteUri, null, null);
	}
}
