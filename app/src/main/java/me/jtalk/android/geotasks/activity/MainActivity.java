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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.Settings;
import me.jtalk.android.geotasks.activity.item.EventElementAdapter;
import me.jtalk.android.geotasks.source.EventsSource;

public class MainActivity extends Activity {
	private static final int LOADER_EVENTS_ID = 0;

	private static final int INTENT_ADD_EVENT = 0;

	private EventsSource eventsSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CursorAdapter eventsAdapter = initEventsList();

		int calendarId = initEventsSource(eventsAdapter);

		Bundle bundle = new Bundle();
		bundle.putInt(EventsSource.BUNDLE_CALENDAR_ID, calendarId);
		getLoaderManager().initLoader(LOADER_EVENTS_ID, bundle, eventsSource);
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

	private int initEventsSource(CursorAdapter eventsAdapter) {
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
		return calendarId;
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
