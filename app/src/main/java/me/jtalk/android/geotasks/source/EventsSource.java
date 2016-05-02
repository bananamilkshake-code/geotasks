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
package me.jtalk.android.geotasks.source;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Reminders;
import android.provider.CalendarContract.Events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import lombok.Getter;
import me.jtalk.android.geotasks.util.CoordinatesFormat;
import me.jtalk.android.geotasks.util.CursorHelper;
import me.jtalk.android.geotasks.util.Logger;

import static java.lang.String.format;
import static me.jtalk.android.geotasks.util.CoordinatesFormat.POINT_ACCURACY;

public class EventsSource implements EventIntentFields {
	public static final Logger LOG = new Logger(EventsSource.class);

	/**
	 * This action will be broadcasted when event has been added, edited or removed.
	 */
	public static final String ACTION_EVENT_CHANGED = "me.jtalk.geotasks.ACTION_EVENT_CHANGED";

	public static final String INTENT_EXTRA_ACTION = "action";

	public static final int ACTION_NONE = 0;
	public static final int ACTION_ADD = 1;
	public static final int ACTION_EDIT = 2;
	public static final int ACTION_REMOVED = 3;

	public static final long DEFAULT_CALENDAR = -1;

	public static final long NO_TASK = -1;

	static final String ACTIVE = "active";

	/**
	 * First argument - value range in location string value
	 * Second argument - column name
	 */
	private static final String QUERY_COORDINATES_FORMAT =
			"(CASE WHEN " + Events.EVENT_LOCATION + " IS NOT NULL " +
					"OR length(" + Events.EVENT_LOCATION + ") = 0 " +
					"THEN CAST(substr(" + Events.EVENT_LOCATION + ", %s) AS REAL) " +
					"ELSE NULL END) " +
					"AS %s";

	private static final String QUERY_ACTIVE_FIELD =
			"(CASE WHEN " + Events.DTEND + " == " + DEFAULT_CALENDAR + " OR " + Events.DTEND  +  "  >= %d THEN 'TRUE'" +
			"ELSE 'FALSE' END) " +
			"AS " + ACTIVE;

	private Context context;

	@Getter
	private long calendarId;

	public static final String[] PROJECTION_EVENTS = new String[]{
			Events._ID,
			Events.CALENDAR_ID,
			Events.TITLE,
			Events.DESCRIPTION,
			Events.EVENT_LOCATION,
			Events.DTSTART,
			Events.DTEND,
			Events.HAS_ALARM,
			format(QUERY_COORDINATES_FORMAT, format("0, %d", POINT_ACCURACY), CursorHelper.EVENT_LATITUDE),
			format(QUERY_COORDINATES_FORMAT, format("%d + 1", POINT_ACCURACY), CursorHelper.EVENT_LONGITUDE)
	};

	private static final String SORT_ORDER = ACTIVE + " DESC";

	public static Loader<Cursor> createCursorLoader(Context context, long calendarId, Calendar currentTime) {
		List<String> projection = new ArrayList<>(Arrays.asList(PROJECTION_EVENTS));
		projection.add(String.format(QUERY_ACTIVE_FIELD, currentTime.getTimeInMillis(), currentTime.getTimeInMillis()));

		String selection = CursorHelper.buildProjection(CalendarContract.Events.CALENDAR_ID);
		String[] selectionArgs = new String[] {
				String.valueOf(calendarId)
				//, String.valueOf(currentTime.getTimeInMillis())
				//, String.valueOf(currentTime.getTimeInMillis())
		};

		return new CursorLoader(
				context,
				CalendarContract.Events.CONTENT_URI,
				PROJECTION_EVENTS, //projection.toArray(new String[projection.size()]),
				selection,
				selectionArgs,
				null); //SORT_ORDER_BY_ACTIVE);
	}

	public EventsSource(Context context, long calendarId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
				throw new SecurityException("No permission to write calendar");
			}
		}

		this.context = context;
		this.calendarId = calendarId;
	}

	/**
	 * Creates new event in Android Calendar Provider.
	 *
	 * @param event to create
	 * @throws SecurityException is thrown if Calendar permission is not granted for application.
	 */
	public void add(Event event) throws SecurityException {
		ContentValues values = createContentValues(event);

		Uri created = this.context.getContentResolver().insert(Events.CONTENT_URI, values);
		if (created == null) {
			LOG.warn("Event is not added");
			return;
		}

		long id = Long.valueOf(created.getLastPathSegment());

		LOG.debug("New event was created. Uri: {0}, id {1}", created.toString(), id);

		sendBroadcast(ACTION_ADD, calendarId, id);

		enable(id);
	}

	/**
	 * Selects event with id {@id}.
	 *
	 * @param id id of event to retrieve
	 * @return retrieved event
	 */
	public Event get(long id) {
		Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, id);

		Cursor cursor = this.context.getContentResolver().query(uri, PROJECTION_EVENTS, null, null, null);
		if (cursor == null || cursor.isAfterLast()) {
			return null;
		}

		cursor.moveToFirst();
		Event event = extractEvent(cursor);

		cursor.close();

		return event;
	}

	/**
	 * Update event
	 *
	 * @param event event to update
	 * @throws SecurityException is thrown if Calendar permission is not granted for application.
	 */
	public void edit(Event event) {
		Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, event.getId());

		ContentValues values = createContentValues(event);

		this.context.getContentResolver().update(uri, values, null, null);

		sendBroadcast(ACTION_EDIT, calendarId, event.getId());
	}

	/**
	 * Remove event with {@id} from Calendars Provider.
	 *
	 * @param id event id to remove
	 */
	public void remove(long id) {
		Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
		this.context.getContentResolver().delete(deleteUri, null, null);
		sendBroadcast(ACTION_REMOVED, calendarId, id);
	}

	/**
	 * Makes event active to enable notifications from it.
	 *
	 * @param id id of event to enable
	 */
	public void enable(long id) throws SecurityException {
		// No need to update HAS_ALARMS in CalendarProvider: this field
		// is changed automatically ith reminders adding/deleting.

		ContentValues reminder = createReminderContentValues(id);
		Uri createdReminder = this.context.getContentResolver().insert(Reminders.CONTENT_URI, reminder);

		LOG.debug("Reminder for event {0} created: uri {1}. Event is active now.", id, createdReminder);
	}

	private static final String REMOVE_EVENT_REMINDERS = "Reminders.EVENT_ID = ?";

	/**
	 * Makes event inactive to prevent notifications from it.
	 *
	 * @param id id of event to disable
	 */
	public void disable(long id) throws SecurityException {
		// No need to update HAS_ALARMS in CalendarProvider: this field
		// is changed automatically ith reminders adding/deleting.
		this.context.getContentResolver().delete(
				Reminders.CONTENT_URI,
				REMOVE_EVENT_REMINDERS,
				new String[]{String.valueOf(id)});

		LOG.debug("Reminders for event {0} from calendar {1} removed. Event is inactive now.", id, calendarId);

		sendBroadcast(ACTION_EDIT, calendarId, id);
	}

	/**
	 * Selects events that must happen around current time or which time
	 * is not set.
	 *
	 * @param currentTime time to check events occurrence
	 * @return list of selected events
	 * @throws SecurityException
	 */
	public List<Event> getActiveLocationEvents(Calendar currentTime) throws SecurityException {
		String[] selectionArgs = new String[]{
				String.valueOf(calendarId)
				, String.valueOf(currentTime.getTimeInMillis())
				, String.valueOf(currentTime.getTimeInMillis())
		};

		Cursor cursor = context.getContentResolver().query(
				Events.CONTENT_URI,
				PROJECTION_EVENTS,
				ACTIVE_EVENTS_SELECTION,
				selectionArgs,
				null);

		List<Event> events = new ArrayList<>();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				events.add(CursorHelper.extractEvent(cursor));
			}
			cursor.close();
		}

		return events;
	}

	private static final String ACTIVE_TIMING_EVENTS_SELECTION =
			Events.CALENDAR_ID + " = ? "
					+ "AND " + Events.HAS_ALARM + " == 1 "
					+ "AND " + Events.EVENT_LOCATION + " IS NULL OR length(" + Events.EVENT_LOCATION + ") == 0 "
					+ "AND " + Events.DTSTART + " >= ?";

	/**
	 * Get events that must be started in future but not in defined location.
	 *
	 * @param currentTime time of method call
	 * @return list of events to notify in special time
	 */
	public List<Event> getActiveTimingEvents(Calendar currentTime) throws SecurityException {
		String[] selectionArgs = new String[]{
				String.valueOf(calendarId),
				String.valueOf(currentTime.getTimeInMillis())
		};

		Cursor cursor = context.getContentResolver().query(
				Events.CONTENT_URI,
				PROJECTION_EVENTS,
				ACTIVE_TIMING_EVENTS_SELECTION,
				selectionArgs,
				null);

		List<Event> events = new ArrayList<>();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				Event event = CursorHelper.extractEvent(cursor);
				events.add(event);
				LOG.debug("Active timing event id {0}: start time {1}, current time {2}, diff {3}",
						event.getId(),
						CursorHelper.getMillis(event.getStartTime()),
						currentTime.getTimeInMillis(),
						CursorHelper.getMillis(event.getStartTime()) - currentTime.getTimeInMillis());
			}
			cursor.close();
		}
		return events;
	}

	private ContentValues createContentValues(Event event) {
		ContentValues values = new ContentValues();
		values.put(Events.CALENDAR_ID, calendarId);
		values.put(Events.TITLE, event.getTitle());
		values.put(Events.DESCRIPTION, event.getDescription());
		values.put(Events.EVENT_LOCATION, CoordinatesFormat.formatForDatabase(event.getCoordinates()));
		values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
		values.put(Events.DTSTART, CursorHelper.getMillis(event.getStartTime()));
		values.put(Events.DTEND, CursorHelper.getMillis(event.getEndTime()));
		return values;
	}

	private static final int MINUTES_BEFORE_START = 1;

	private ContentValues createReminderContentValues(long eventId) {
		ContentValues values = new ContentValues();
		values.put(Reminders.MINUTES, MINUTES_BEFORE_START);
		values.put(Reminders.EVENT_ID, eventId);
		values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
		return values;
	}

	private static final String ACTIVE_EVENTS_SELECTION =
			Events.CALENDAR_ID + " = ? "
			+ "AND " + Events.HAS_ALARM + " == 1 "
			+ "AND " + Events.EVENT_LOCATION + " IS NOT NULL AND length(" + Events.EVENT_LOCATION + ") <> 0 "
			+ "AND (" + Events.DTSTART + " == -1 OR " + Events.DTSTART + " >= ?) "
			+ "AND (" + Events.DTEND + " == -1 OR " + Events.DTEND + " < ?)";

	private static String[] buildSelectionArgsForNearEvents(long calendarId, Calendar currentTime) {
		return new String[]{
				String.valueOf(calendarId)
				, String.valueOf(currentTime.getTimeInMillis())
				, String.valueOf(currentTime.getTimeInMillis())
		};
	}

	private void sendBroadcast(int action, long calendarId, long eventId) {
		LOG.debug("Notifying about event change: action {0}, calendarId {1}, eventId {2}", action, calendarId, eventId);
		Intent intent = new Intent(ACTION_EVENT_CHANGED);
		intent.putExtra(INTENT_EXTRA_ACTION, action);
		intent.putExtra(INTENT_EXTRA_CALENDAR_ID, calendarId);
		intent.putExtra(INTENT_EXTRA_EVENT_ID, eventId);
		context.sendBroadcast(intent);
	}
}
