package me.jtalk.android.geotasks.application.callbacks;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.CursorAdapter;

import lombok.AllArgsConstructor;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.CalendarHelper;
import me.jtalk.android.geotasks.util.Logger;

/**
 * Listens for events changes and updates data in events adapter.
 */
@AllArgsConstructor
public class TasksLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final Logger LOG = new Logger(TasksLoaderCallbacks.class);

	private Context context;

	private CursorAdapter tasksAdapter;

	private long calendarId;

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		LOG.debug("TasksLoaderCallbacks creates loader for {0} calendar", calendarId);

		String selection = CalendarHelper.buildProjection(CalendarContract.Events.CALENDAR_ID);
		String[] selectionArgs = new String[]{String.valueOf(calendarId)};

		return new CursorLoader(context, CalendarContract.Events.CONTENT_URI,
				EventsSource.PROJECTION_EVENTS, selection, selectionArgs, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		tasksAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		tasksAdapter.swapCursor(null);
	}
}
