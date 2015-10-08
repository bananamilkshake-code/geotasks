package me.jtalk.android.geotasks.calendar;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.CursorAdapter;

public class CalendarSource implements LoaderManager.LoaderCallbacks<Cursor> {

	private Context context;

	private CursorAdapter calendarsAdapter;

	private static final String[] PROJECTION_CALENDARS = new String[] {
			CalendarContract.Calendars._ID,
			CalendarContract.Calendars.NAME,
			CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
			CalendarContract.Calendars.ACCOUNT_NAME,
			CalendarContract.Calendars.ACCOUNT_TYPE,
			CalendarContract.Calendars.OWNER_ACCOUNT
	};

	public CalendarSource(Context context, CursorAdapter calendarsAdapter) {
		this.context = context;
		this.calendarsAdapter = calendarsAdapter;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(context,
				CalendarContract.Calendars.CONTENT_URI,
				PROJECTION_CALENDARS, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		calendarsAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		calendarsAdapter.swapCursor(null);
	}

	public void addCalendar(String name) throws SecurityException {
		ContentValues values = new ContentValues();
		values.put(CalendarContract.Calendars.NAME, name);
		values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name);
		values.put(CalendarContract.Calendars.VISIBLE, true);
		values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);

		this.context.getContentResolver().insert(CalendarContract.Calendars.CONTENT_URI, values);
	}
}
