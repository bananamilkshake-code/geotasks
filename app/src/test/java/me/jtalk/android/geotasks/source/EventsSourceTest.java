package me.jtalk.android.geotasks.source;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.CalendarContract;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.RealObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import me.jtalk.android.geotasks.BuildConfig;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.util.CoordinatesFormat;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
		sdk = 21,
		manifest = "src/main/AndroidManifest.xml")
public class EventsSourceTest {
	final static long calendarId = 1;

	private EventsSource eventsSource;

	@Mock
	private ContentResolver mockedContentResolver;

	@Mock
	private Context mockedContext;

	@RealObject
	private MatrixCursor cursor;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(mockedContext.getContentResolver()).thenReturn(mockedContentResolver);

		eventsSource = new EventsSource(mockedContext, calendarId);
	}

	@Test
	public void testEventCreation() throws SecurityException {
		TaskCoordinates coordinates = new TaskCoordinates(12.3434, 54.23434);

		final String title = "Title";
		final String description = "Description";
		final String location = CoordinatesFormat.prettyFormat(coordinates);
		final Calendar startTime = Calendar.getInstance();
		final Calendar endTime = Calendar.getInstance();

		startTime.set(2015, 11, 19);
		endTime.set(2015, 11, 20);

		ContentValues expectedValues = new ContentValues();
		expectedValues.put(CalendarContract.Events.CALENDAR_ID, calendarId);
		expectedValues.put(CalendarContract.Events.TITLE, title);
		expectedValues.put(CalendarContract.Events.DESCRIPTION, description);
		expectedValues.put(CalendarContract.Events.EVENT_LOCATION, location);
		expectedValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
		expectedValues.put(CalendarContract.Events.DTSTART, startTime.getTimeInMillis());
		expectedValues.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());

		when(mockedContentResolver.insert(CalendarContract.Events.CONTENT_URI, expectedValues))
				.thenReturn(new Uri.Builder().build());

		Event event = new Event(-1, title, description, startTime, endTime, coordinates);
		eventsSource.add(event);

		verify(mockedContext).getContentResolver();
		verify(mockedContentResolver).insert(CalendarContract.Events.CONTENT_URI, expectedValues);
	}

	@Test
	public void testEventDestruction() throws SecurityException {
		long eventId = 1;

		when(mockedContentResolver.delete(any(Uri.class), any(String.class), any(String[].class)))
				.thenReturn(1);

		eventsSource.remove(eventId);

		Uri expectedDeleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);

		final String expectedWhere = null;
		final String[] expectedSelectionArgs = null;

		verify(mockedContext).getContentResolver();
		verify(mockedContentResolver).delete(expectedDeleteUri, expectedWhere, expectedSelectionArgs);
	}

	@Test
	public void testGetActive() {
		final Calendar eventStartTime = Calendar.getInstance();
		eventStartTime.set(2015, 11, 12);

		final Calendar eventEndTime = Calendar.getInstance();
		eventEndTime.set(2015, 11, 12);
		Event event1 = new Event(1, "Title1", "Description1", eventStartTime, eventEndTime, new TaskCoordinates(1, 1));
		Event event2 = new Event(2, "Title2", "Description1", eventStartTime, eventEndTime, new TaskCoordinates(2, 2));
		Event event3 = new Event(3, "Title3", "Description1", eventStartTime, eventEndTime, new TaskCoordinates(3, 3));

		Cursor cursor = createCursor(event1, event2, event3);

		when(mockedContentResolver.query(
				any(Uri.class),
				any(String[].class),
				any(String.class),
				any(String[].class),
				any(String.class)))
				.thenReturn(cursor);

		final Calendar currentTime = Calendar.getInstance();
		currentTime.set(2015, 11, 21);

		assertEquals(eventsSource.getActive(currentTime), Arrays.asList(event1, event2, event3));
	}

	private Cursor createCursor(Event... events) {
		int rowNumber = 3;
		cursor = new MatrixCursor(new String[]{
				CalendarContract.Events._ID,
				CalendarContract.Events.TITLE,
				CalendarContract.Events.DESCRIPTION,
				CalendarContract.Events.EVENT_LOCATION,
				CalendarContract.Events.DTSTART,
				CalendarContract.Events.DTEND,
				EventsSource.EVENT_LATITUDE,
				EventsSource.EVENT_LONGITUDE},
				rowNumber);

		for (Event event : events) {
			cursor.newRow()
					.add(CalendarContract.Events._ID, event.getId())
					.add(CalendarContract.Events.TITLE, event.getTitle())
					.add(CalendarContract.Events.DESCRIPTION, event.getDescription())
					.add(CalendarContract.Events.EVENT_LOCATION, CoordinatesFormat.prettyFormat(event.getCoordinates()))
					.add(CalendarContract.Events.DTSTART, event.getStartTime().getTimeInMillis())
					.add(CalendarContract.Events.DTEND, event.getEndTime().getTimeInMillis())
					.add(EventsSource.EVENT_LATITUDE, event.getCoordinates().getLatitude())
					.add(EventsSource.EVENT_LONGITUDE, event.getCoordinates().getLongitude());
		}

		return cursor;
	}
}
