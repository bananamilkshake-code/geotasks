package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.Settings;
import me.jtalk.android.geotasks.activity.item.EventElementAdapter;
import me.jtalk.android.geotasks.source.EventsSource;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getName();

	private static final int LOADER_EVENTS_ID = 0;

	private static final int INTENT_ADD_EVENT = 0;

	private EventsSource eventsSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CursorAdapter eventsAdapter = initEventsList();
		initEventsSource(eventsAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
			case INTENT_ADD_EVENT:
				onAddEventResult(data);
				return;
		}
	}

	// This method is called on menu.menu_action_add_event click
	public boolean openAddEventIntent(MenuItem menuItem) {
		startActivityForResult(new Intent(this, AddEventActivity.class), INTENT_ADD_EVENT);
		return true;
	}

	private CursorAdapter initEventsList() {
		CursorAdapter eventsAdapter = new EventElementAdapter(this);

		ListView eventsList = (ListView) findViewById(R.id.events_list);
		eventsList.setAdapter(eventsAdapter);

		eventsList.setOnItemLongClickListener((parent, view, position, id) -> {
			long eventId = parent.getAdapter().getItemId(position);
			String eventTitle = ((TextView) view.findViewById(R.id.event_element_title)).getText().toString();

			new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.dialog_delete_event_title)
					.setMessage(String.format(getString(R.string.dialog_delete_event_text), eventTitle))
					.setPositiveButton(R.string.dialog_delete_event_yes, (dialog, which) -> {
						MainActivity.this.eventsSource.removeEvent(eventId);
					})
					.setNegativeButton(R.string.dialog_delete_event_no, null)
					.setCancelable(true)
					.show();
			return true;
		});

		return eventsAdapter;
	}

	private void initEventsSource(CursorAdapter eventsAdapter) {
		long calendarId = getCalendarId();

		Log.i(TAG, String.format("Application will use calendar %d", calendarId));

		eventsSource = new EventsSource(this, eventsAdapter, calendarId);

		getLoaderManager().initLoader(LOADER_EVENTS_ID, null, eventsSource);
	}

	private long getCalendarId() {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);

		long calendarId = settings.getLong(Settings.CALENDAR_ID, Settings.DEFAULT_CALENDAR);
		if (calendarId != Settings.DEFAULT_CALENDAR) {
			return calendarId;
		}

		Log.i(TAG, "No calendar defined in settings. Ceating new calendar.");
		calendarId = createNewCalendar();

		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Settings.CALENDAR_ID, calendarId);
		editor.commit();

		return calendarId;
	}

	private long createNewCalendar() {
		ContentValues values = new ContentValues();
		values.put(CalendarContract.Calendars.NAME, "GeoTasks calendar");
		values.put(CalendarContract.Calendars.VISIBLE, true);

		Uri uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, Boolean.TRUE.toString())
				.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "GeoTasks account")
				.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();

		Uri inserted = getContentResolver().insert(uri, values);

		long id = Integer.valueOf(inserted.getLastPathSegment());

		Log.d(TAG, String.format("Calendar with id %d created", id));
		return id;
	}

	private void onAddEventResult(Intent data) {
		String eventTitle = data.getStringExtra(AddEventActivity.EXTRA_TITLE);
		String eventDescription = data.getStringExtra(AddEventActivity.EXTRA_DESCRIPTION);
		long startTime = data.getLongExtra(AddEventActivity.EXTRA_START_TIME, Settings.DEFAULT_START_TIME);
		String timezone = data.getStringExtra(AddEventActivity.EXTRA_TIMEZONE);
		eventsSource.addEvent(eventTitle, eventDescription, startTime, timezone);
	}
}
