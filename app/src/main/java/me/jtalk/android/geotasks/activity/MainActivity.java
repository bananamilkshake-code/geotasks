package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.Settings;
import me.jtalk.android.geotasks.source.EventsSource;

public class MainActivity extends Activity {
	private static final int LOADER_EVENTS_ID = 0;

	private static final int INTENT_ADD_EVENT = 0;

	private EventsSource eventsSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SimpleCursorAdapter eventsAdapter = initEventsList();

		initEventsSource(eventsAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		menu.findItem(R.id.action_add_event).setOnMenuItemClickListener(item -> {
			startActivityForResult(new Intent(this, AddEventActivity.class), INTENT_ADD_EVENT);
			return true;
		});

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

	private SimpleCursorAdapter initEventsList() {
		SimpleCursorAdapter eventsAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, null,
				new String[] {CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION},
				new int[] {android.R.id.text1, android.R.id.text2},
				0);

		ListView eventsList = (ListView) findViewById(R.id.events_list);
		eventsList.setAdapter(eventsAdapter);

		return eventsAdapter;
	}

	private void initEventsSource(SimpleCursorAdapter eventsAdapter) {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);

		int calendarId = settings.getInt(Settings.CALENDAR_ID, Settings.DEFAULT_CALENDAR);
		if (calendarId == Settings.DEFAULT_CALENDAR) {
			Log.i(MainActivity.class.getName(), "No calendar defined in settings. Ceating new calendar.");
			calendarId = createNewCalendar();

			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(Settings.CALENDAR_ID, calendarId);
			editor.commit();
		}

		Log.i(MainActivity.class.getName(), String.format("Application will use calendar %d", calendarId));

		eventsSource = new EventsSource(this, eventsAdapter);

		Bundle bundle = new Bundle();
		bundle.putInt(EventsSource.BUNDLE_CALENDAR_ID, calendarId);
		getLoaderManager().initLoader(LOADER_EVENTS_ID, bundle, eventsSource);
	}

	private int createNewCalendar() {
		ContentValues values = new ContentValues();
		values.put(CalendarContract.Calendars.NAME, "GeoTasks calendar");
		values.put(CalendarContract.Calendars.VISIBLE, true);

		Uri uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, Boolean.TRUE.toString())
				.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "GeoTasks account")
				.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();

		Uri inserted = getContentResolver().insert(uri, values);

		int id = Integer.valueOf(inserted.getLastPathSegment());

		Log.d(MainActivity.class.getName(), String.format("Calendar with id %d created", id));
		return id;
	}

	private void onAddEventResult(Intent data) {
		String eventTitle = data.getStringExtra(AddEventActivity.EXTRA_TITLE);
		String eventDescription = data.getStringExtra(AddEventActivity.EXTRA_DESCRIPTION);
		eventsSource.addEvent(eventTitle, eventDescription);
	}
}
