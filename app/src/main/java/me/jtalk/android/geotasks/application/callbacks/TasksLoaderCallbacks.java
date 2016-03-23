/*
 * Copyright (C) 2016 Liza Lukicheva
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package me.jtalk.android.geotasks.application.callbacks;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.CursorTreeAdapter;

import lombok.AllArgsConstructor;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.CursorHelper;
import me.jtalk.android.geotasks.util.Logger;

/**
 * Listens for events changes and updates data in events adapter.
 */
@AllArgsConstructor
public class TasksLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final Logger LOG = new Logger(TasksLoaderCallbacks.class);

	private Context context;

	private CursorTreeAdapter tasksAdapter;

	private long calendarId;

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		LOG.debug("TasksLoaderCallbacks creates loader for {0} calendar", calendarId);

		String selection = CursorHelper.buildProjection(CalendarContract.Events.CALENDAR_ID);
		String[] selectionArgs = new String[]{String.valueOf(calendarId)};

		return new CursorLoader(context, CalendarContract.Events.CONTENT_URI,
				EventsSource.PROJECTION_EVENTS, selection, selectionArgs, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		tasksAdapter.changeCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		tasksAdapter.changeCursor(null);
	}
}
