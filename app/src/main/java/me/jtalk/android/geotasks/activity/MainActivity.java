package me.jtalk.android.geotasks.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.Settings;
import me.jtalk.android.geotasks.activity.item.EventElementAdapter;
import me.jtalk.android.geotasks.source.CalendarsSource;
import me.jtalk.android.geotasks.source.EventsSource;

public class MainActivity extends BaseActivity {
	private static final String TAG = MainActivity.class.getName();

	private static final int LOADER_EVENTS_ID = 0;

	private static final int PERMISSION_REQUEST_CREATE_CALENDAR = 0;
	private static final int PERMISSION_REQUEST_READ_EVENTS = 1;

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
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] values) {
		if (values == null) {
			// interrupted by user
			Log.d(TAG, "Permission request was interrupted by user");
			return;
		}

		switch (requestCode) {
			case PERMISSION_REQUEST_CREATE_CALENDAR:
				onCreateCalendarPermissionGranted(permissions, values);
				return;
			case PERMISSION_REQUEST_READ_EVENTS:
				onReadEventsPermission(permissions, values);
				return;
		}
	}

	private void onCreateCalendarPermissionGranted(String[] permissions, int[] values) {
		if (checkGranted(Manifest.permission.WRITE_CALENDAR, permissions, values)) {
			ListView eventsList = (ListView) findViewById(R.id.events_list);
			initEventsSource((CursorAdapter) eventsList.getAdapter());
		} else {
			onNoPermissionError();
		}
	}

	private void onReadEventsPermission(String[] permissions, int[] values) {
		if (checkGranted(Manifest.permission.WRITE_CALENDAR, permissions, values)) {
			ListView eventsList = (ListView) findViewById(R.id.events_list);
			setupEventsSource(getCalendarId(), (CursorAdapter) eventsList.getAdapter());
		} else {
			onNoPermissionError();
		}
	}

	// This method is called on menu.menu_action_add_event click
	public boolean openAddEventIntent(MenuItem menuItem) {
		startActivity(new Intent(this, AddEventActivity.class));
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
						MainActivity.this.getEventsSource().removeEvent(eventId);
					})
					.setNegativeButton(R.string.dialog_delete_event_no, null)
					.setCancelable(true)
					.show();
			return true;
		});

		return eventsAdapter;
	}

	private void initEventsSource(CursorAdapter eventsAdapter) {
		long calendarId;
		try {
			calendarId = getCalendarId();

			Log.i(TAG, String.format("Application will use calendar %d", calendarId));
		} catch (SecurityException exception) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_CREATE_CALENDAR);
			} else {
				onNoPermissionError();
			}
			return;
		}

		try {
			setupEventsSource(calendarId, eventsAdapter);
		} catch (SecurityException exception) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_READ_EVENTS);
			} else {
				onNoPermissionError();
			}
			return;
		}
	}

	private void setupEventsSource(long calendarId, CursorAdapter eventsAdapter) {
		EventsSource eventsSource = new EventsSource(this, eventsAdapter, calendarId);

		getLoaderManager().initLoader(LOADER_EVENTS_ID, null, eventsSource);

		setEventsSource(eventsSource);
	}

	private long getCalendarId() throws SecurityException {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);

		long calendarId = settings.getLong(Settings.CALENDAR_ID, Settings.DEFAULT_CALENDAR);
		if (calendarId != Settings.DEFAULT_CALENDAR) {
			return calendarId;
		}

		Log.i(TAG, "No calendar defined in settings. Creating new calendar.");
		calendarId = new CalendarsSource(this).addCalendar();

		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Settings.CALENDAR_ID, calendarId);
		editor.commit();

		return calendarId;
	}
}
