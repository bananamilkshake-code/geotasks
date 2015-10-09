package me.jtalk.android.geotasks.source;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.widget.CursorAdapter;

import com.google.common.base.Preconditions;

import java.util.Calendar;
import java.util.TimeZone;

import me.jtalk.android.geotasks.util.CalendarHelper;

import static me.jtalk.android.geotasks.util.Assert.verifyArgument;

public class EventsSource implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String BUNDLE_CALENDAR_ID = "calendar-id";

    private Context context;
    private CursorAdapter eventsAdapter;

    private int calendarId;

    private static final String[] PROJECTION_EVENTS = new String[] {
            Events._ID,
            Events.CALENDAR_ID,
            Events.TITLE,
            Events.DESCRIPTION,
            Events.EVENT_LOCATION
    };

    public EventsSource(Context context, CursorAdapter eventsAdapter) {
        this.context = context;
        this.eventsAdapter = eventsAdapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        calendarId = args.getInt(BUNDLE_CALENDAR_ID, -1);

        verifyArgument(calendarId != -1, "No calendarId transfered");

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

    public void addEvent(String title) throws SecurityException {
        ContentValues values = new ContentValues();
        values.put(Events.CALENDAR_ID, calendarId);
        values.put(Events.DTSTART, Calendar.getInstance().getTimeInMillis());
        values.put(Events.DTEND, Calendar.getInstance().getTimeInMillis() + 1000 * 5 * 24 * 60 * 60);
        values.put(Events.EVENT_TIMEZONE, TimeZone.getAvailableIDs()[0]);
        values.put(Events.TITLE, title);

        this.context.getContentResolver().insert(Events.CONTENT_URI, values);
    }
}
