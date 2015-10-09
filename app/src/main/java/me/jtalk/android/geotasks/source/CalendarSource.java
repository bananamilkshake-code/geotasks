package me.jtalk.android.geotasks.source;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CursorAdapter;

import static android.provider.CalendarContract.*;

public class CalendarSource implements LoaderManager.LoaderCallbacks<Cursor> {
	private Context context;
	private CursorAdapter calendarsAdapter;

	private static final String[] PROJECTION_CALENDARS = new String[] {
			Calendars._ID,
			Calendars.NAME,
			Calendars.CALENDAR_DISPLAY_NAME,
			Calendars.ACCOUNT_NAME,
			Calendars.ACCOUNT_TYPE,
			Calendars.OWNER_ACCOUNT
	};

	public CalendarSource(Context context, CursorAdapter calendarsAdapter) {
		this.context = context;
		this.calendarsAdapter = calendarsAdapter;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(context,
				Calendars.CONTENT_URI,
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
		values.put(Calendars.NAME, name);
		values.put(Calendars.CALENDAR_DISPLAY_NAME, name);
		values.put(Calendars.VISIBLE, true);

		this.context.getContentResolver().insert(asSyncAdapter(ACCOUNT_TYPE_LOCAL), values);
	}

	static Uri asSyncAdapter(String accountType) {
		return Calendars.CONTENT_URI.buildUpon()
				.appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, "aa")
				.appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
	}
}
