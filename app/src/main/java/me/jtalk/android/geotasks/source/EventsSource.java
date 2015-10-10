package me.jtalk.android.geotasks.source;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.widget.CursorAdapter;

import java.util.TimeZone;

import me.jtalk.android.geotasks.Settings;
import me.jtalk.android.geotasks.util.CalendarHelper;

import static me.jtalk.android.geotasks.util.Assert.verifyArgument;

public class EventsSource implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String BUNDLE_CALENDAR_ID = "calendar-id";

    private Context context;
    private CursorAdapter eventsAdapter;

    private int calendarId = Settings.DEFAULT_CALENDAR;

    private static final String[] PROJECTION_EVENTS = new String[] {
            Events._ID,
            Events.CALENDAR_ID,
            Events.TITLE,
            Events.DESCRIPTION,
            Events.EVENT_LOCATION,
            Events.DTSTART
    };

    public EventsSource(Context context, CursorAdapter eventsAdapter) {
        this.context = context;
        this.eventsAdapter = eventsAdapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        calendarId = args.getInt(BUNDLE_CALENDAR_ID, Settings.DEFAULT_CALENDAR);

        verifyArgument(calendarId != -1, "No calendarId transferred");

        Log.d(EventsSource.class.getName(), String.format("EventsSource created loader for %d calendar", calendarId));

        String selection = CalendarHelper.buildProjection(Events.CALENDAR_ID);
        String[] selectionArgs = new String[] { String.valueOf(calendarId) };

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

    public void addEvent(String title, String description) throws SecurityException {
        ContentValues values = new ContentValues();
        values.put(Events.CALENDAR_ID, calendarId);
        values.put(Events.TITLE, title);
        values.put(Events.DESCRIPTION, description);

        values.put(Events.DTSTART, -1);
        values.put(Events.DTEND, -1);
        values.put(Events.EVENT_TIMEZONE, TimeZone.getAvailableIDs()[0]);

        this.context.getContentResolver().insert(Events.CONTENT_URI, values);
    }
}
