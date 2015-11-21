package me.jtalk.android.geotasks.source;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract.Events;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.util.CalendarHelper;
import me.jtalk.android.geotasks.util.Logger;

import static java.lang.String.format;
import static me.jtalk.android.geotasks.util.CoordinatesFormat.POINT_ACCURACY;

public class EventsSource {
	public static final Logger LOG = new Logger(EventsSource.class);

	public static final long DEFAULT_CALENDAR = -1;
	public static final long DEFAULT_TIME_VALUE = -1;

	public static final Calendar EMPTY_TIME = null;

	/**
	 * First argument - value range in location string value
	 * Second argument - column name
	 */
	private static final String QUERY_COORDINATES_FORMAT =
			"(CASE WHEN " + Events.EVENT_LOCATION + " IS NOT NULL " +
					"OR length(" + Events.EVENT_LOCATION + ") = 0 " +
					"THEN CAST(substr(" + Events.EVENT_LOCATION + ", %s) AS REAL) " +
					"ELSE NULL END)" +
					"AS %s";

	static final String EVENT_LATITUDE = "latitude";
	static final String EVENT_LONGITUDE = "longitude";

	private Context context;

	private long calendarId;

	public static final String[] PROJECTION_EVENTS = new String[]{
			Events._ID,
			Events.CALENDAR_ID,
			Events.TITLE,
			Events.DESCRIPTION,
			Events.EVENT_LOCATION,
			Events.DTSTART,
			format(QUERY_COORDINATES_FORMAT, format("0, %d", POINT_ACCURACY), EVENT_LATITUDE),
			format(QUERY_COORDINATES_FORMAT, format("%d + 1", POINT_ACCURACY), EVENT_LONGITUDE)
	};

	/**
	 * Exracts data for event from cursor (projection fields are {@link PROJECTION_EVENTS})
	 * and creates Event object from it.
	 *
	 * @param cursor
	 * @return Event object that was created from retrieved data.
	 */
	public static Event extractEvent(Cursor cursor) {
		long id = CalendarHelper.getLong(cursor, Events._ID);
		String title = CalendarHelper.getString(cursor, Events.TITLE);
		Calendar startTime = getTimeText(cursor, Events.DTSTART);
		TaskCoordinates geoPoint = getCoordinates(cursor);
		return new Event(id, title, startTime, geoPoint);
	}

	/**
	 * Retrieves column value from cursor and converts it to calendar.
	 * If start time value is not been set for that event null will be returned.
	 *
	 * @param cursor
	 * @param timeField field name that contains time value (long)
	 * @return calendar with start time value or null if time hadn't been set
	 */
	public static Calendar getTimeText(Cursor cursor, String timeField) {
		long timeInMillis = CalendarHelper.getLong(cursor, timeField);
		if (timeInMillis == DEFAULT_TIME_VALUE) {
			return null;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		return calendar;
	}

	/**
	 * Safety retrieves millis from calendar: null calendar means that no time is set
	 * and {@DEFAULT_TIME_VALUE} must be returned.
	 *
	 * @param time
	 * @return time in milliseconds or @{DEFAULT_TIME_VALUE} if time is null
	 */
	private static long getMillis(Calendar time) {
		return (time != EMPTY_TIME) ? time.getTimeInMillis() : DEFAULT_TIME_VALUE;
	}

	/**
	 * Retrieves location coordinates where event, which data is stored in current cursor,
	 * must occur. If no location coordinates had been set null will be returned.
	 *
	 * @param cursor
	 * @return TaskCoordinates object that contains information about longitude and latitude.
	 * Returns null if location for event has not been set.
	 */
	private static TaskCoordinates getCoordinates(Cursor cursor) {
		if (CalendarHelper.getString(cursor, Events.EVENT_LOCATION).isEmpty()) {
			return null;
		}

		double lat = CalendarHelper.getDouble(cursor, EVENT_LATITUDE);
		double lon = CalendarHelper.getDouble(cursor, EVENT_LONGITUDE);
		return new TaskCoordinates(lat, lon);
	}

	public EventsSource(Context context, long calendarId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
				throw new SecurityException("No permission to write calendar");
			}
		}

		this.context = context;
		this.calendarId = calendarId;

		LOG.debug("EventSource {} is created for context {}", this, context);
	}

	/**
	 * Creates new event in Android Calendar Provider.
	 *
	 * @param title
	 * @param description
	 * @param location
	 * @param startTime   event start time. This value can be null if start time is not set.
	 * @param endTime     event end time. This value can be null if start time is not set.
	 * @throws SecurityException is thrown if Calendar permission is not granted for application.
	 */
	public void add(String title, String description, String location, Calendar startTime, Calendar endTime) throws SecurityException {
		LOG.debug("Inserting new event for calendarId {}", calendarId);

		ContentValues values = new ContentValues();
		values.put(Events.CALENDAR_ID, calendarId);
		values.put(Events.TITLE, title);
		values.put(Events.DESCRIPTION, description);
		values.put(Events.EVENT_LOCATION, location);
		values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

		values.put(Events.DTSTART, getMillis(startTime));
		values.put(Events.DTEND, getMillis(endTime));

		Uri created = this.context.getContentResolver().insert(Events.CONTENT_URI, values);
		LOG.debug("New event was created. Uri: {}", created.toString());
	}

	/**
	 * Remove event with {@id} from Calendars Provider.
	 *
	 * @param id event id to remove
	 */
	public void remove(long id) {
		Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
		this.context.getContentResolver().delete(deleteUri, null, null);
	}

	/**
	 * Selects events that must happen around current time or which time
	 * is not set.
	 *
	 * @param currentTime time to check events occurance
	 * @return list of selected events
	 * @throws SecurityException
	 */
	public List<Event> getActive(Calendar currentTime) throws SecurityException {
		String[] selectionArgs = buildSelectionArgsForNearEvents(calendarId, currentTime);

		Cursor cursor = context.getContentResolver().query(
				Events.CONTENT_URI,
				PROJECTION_EVENTS,
				NEAR_EVENTS_SELECTION,
				selectionArgs,
				null);

		List<Event> events = new ArrayList<>();
		while (!cursor.isAfterLast()) {
			events.add(extractEvent(cursor));
			cursor.moveToNext();
		}

		return events;
	}

	private final String NEAR_EVENTS_SELECTION = Events.CALENDAR_ID + " = ? AND " +
			Events.EVENT_LOCATION + " IS NOT NULL AND " +
			"length(" + Events.EVENT_LOCATION + ") <> 0 AND " +
			Events.DTSTART + " >= ? AND " +
			Events.DTEND + " < ?";

	private static String[] buildSelectionArgsForNearEvents(long calendarId, Calendar currentTime) {
		return new String[]{
				String.valueOf(calendarId),
				String.valueOf(currentTime.getTimeInMillis()),
				String.valueOf(currentTime.getTimeInMillis())
		};
	}
}
