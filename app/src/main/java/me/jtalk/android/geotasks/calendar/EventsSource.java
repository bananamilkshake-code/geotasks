package me.jtalk.android.geotasks.calendar;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.CursorAdapter;

import me.jtalk.android.geotasks.util.CalendarHelper;

public class EventsSource implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String BUNDLE_CALENDAR_ID = "calendar-id";

    private Context context;
    private CursorAdapter eventsAdapter;

    private static final String[] PROJECTION_EVENTS = new String[] {
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION
    };

    public EventsSource(Context context, CursorAdapter eventsAdapter) {
        this.context = context;
        this.eventsAdapter = eventsAdapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int calendarId = args.getInt(BUNDLE_CALENDAR_ID);

        String selection = CalendarHelper.buildProjection(CalendarContract.Events.CALENDAR_ID);
        String[] selectionArgs = new String[] { String.valueOf(calendarId) };

        return new CursorLoader(context,
                CalendarContract.Events.CONTENT_URI,
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
}
