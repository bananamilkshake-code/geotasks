package me.jtalk.android.geotasks.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.MessageFormat;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.Settings;
import me.jtalk.android.geotasks.activity.item.EventElementAdapter;
import me.jtalk.android.geotasks.source.CalendarsSource;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.PermissionDependantTask;
import me.jtalk.android.geotasks.util.TasksChain;

public class MainActivity extends BaseActivity {
	private static final String TAG = MainActivity.class.getName();

	private static final int LOADER_EVENTS_ID = 0;

	TasksChain<PermissionDependantTask> initChain;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initChain = new TasksChain<PermissionDependantTask>()
				.addTask(makeTask(() -> getCalendarId(), Manifest.permission.WRITE_CALENDAR))
				.addTask(makeTask(() -> initEventsList(), Manifest.permission.READ_CALENDAR))
				.addTask(makeTask(() -> initEventsSource(), Manifest.permission.READ_CALENDAR));

		processChain(initChain);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] values) {
		processPermissionRequestResult(initChain, requestCode, permissions, values);
	}

	@Override
	protected void onNeededPermissionDenied() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_no_permission_for_calendar_creation_title)
				.setMessage(R.string.dialog_no_permission_for_calendar_creation_message)
				.setPositiveButton(R.string.dialog_no_permission_for_calendar_creation_button, (dialog, which) -> {
					dialog.dismiss();
					MainActivity.this.finish();
				})
				.show();
	}

	// This method is called on menu.menu_action_add_event click
	public boolean openAddEventIntent(MenuItem menuItem) {
		startActivity(new Intent(this, AddEventActivity.class));
		return true;
	}

	private long getCalendarId() throws SecurityException {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);

		long calendarId = settings.getLong(Settings.CALENDAR_ID, EventsSource.DEFAULT_CALENDAR);
		if (calendarId != EventsSource.DEFAULT_CALENDAR) {
			return calendarId;
		}

		Log.i(TAG, "No calendar defined in settings. Creating new calendar.");
		calendarId = new CalendarsSource(this).addCalendar();

		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Settings.CALENDAR_ID, calendarId);
		editor.commit();

		return calendarId;
	}

	private void initEventsList() {
		CursorAdapter eventsAdapter = new EventElementAdapter(this);

		ListView eventsList = (ListView) findViewById(R.id.events_list);
		eventsList.setAdapter(eventsAdapter);

		eventsList.setOnItemLongClickListener((parent, view, position, id) -> {
			long eventId = parent.getAdapter().getItemId(position);
			String eventTitle = ((TextView) view.findViewById(R.id.event_element_title)).getText().toString();

			new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.dialog_delete_event_title)
					.setMessage(MessageFormat.format(getString(R.string.dialog_delete_event_text), eventTitle))
					.setPositiveButton(R.string.dialog_delete_event_yes, (dialog, which) -> {
						MainActivity.this.getEventsSource().removeEvent(eventId);
					})
					.setNegativeButton(R.string.dialog_delete_event_no, null)
					.setCancelable(true)
					.show();
			return true;
		});
	}

	private void initEventsSource() {
		long calendarId = getCalendarId();

		ListView eventsList = (ListView) findViewById(R.id.events_list);
		CursorAdapter eventsAdapter = (CursorAdapter) eventsList.getAdapter();

		EventsSource eventsSource = new EventsSource(this, eventsAdapter, calendarId);

		getLoaderManager().initLoader(LOADER_EVENTS_ID, null, eventsSource);

		setEventsSource(eventsSource);
	}
}
