package me.jtalk.android.geotasks.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.text.MessageFormat;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.Settings;
import me.jtalk.android.geotasks.activity.item.EventElementAdapter;
import me.jtalk.android.geotasks.application.listeners.EventsLocationListener;
import me.jtalk.android.geotasks.application.Notifier;
import me.jtalk.android.geotasks.application.callbacks.TasksLoaderCallbacks;
import me.jtalk.android.geotasks.source.CalendarsSource;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;
import me.jtalk.android.geotasks.util.PermissionDependentTask;
import me.jtalk.android.geotasks.util.TasksChain;

import static me.jtalk.android.geotasks.R.string.pref_is_geolistening_enabled;

public class MainActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final Logger LOG = new Logger(MainActivity.class);

	private static final int LOADER_EVENTS_ID = 0;

	private TasksChain<PermissionDependentTask> initChain;
	private TasksChain<PermissionDependentTask> toggleGeoListenChain;

	private EventsLocationListener locationListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initChain = new TasksChain<PermissionDependentTask>()
				.add(makeTask(this::createLocationListener))
				.add(makeTask(this::getCalendarId, Manifest.permission.WRITE_CALENDAR))
				.add(makeTask(this::initEventsList, Manifest.permission.READ_CALENDAR))
				.add(makeTask(this::initEventsSource, Manifest.permission.READ_CALENDAR));

		toggleGeoListenChain = new TasksChain<PermissionDependentTask>()
				.add(makeTask(this::toggleGeoListening, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION));

		processChain(initChain);

		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		locationListener.setMenuItem(menu.findItem(R.id.menu_action_enable_geolistening));

		processChain(toggleGeoListenChain);

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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key == getString(R.string.pref_is_geolistening_enabled)) {
			processChain(toggleGeoListenChain);
		}
	}

	/**
	 * This method is called on menu.menu_action_enable_geolistening click.
	 *
	 * @param menuItem
	 * @return
	 */
	public boolean openAddEventIntent(MenuItem menuItem) {
		startActivity(new Intent(this, MakeTaskActivity.class));
		return true;
	}

	/**
	 * This method is called on menu.menu_action_add_event click.
	 *
	 * @param menuItem
	 * @return
	 */
	public boolean toggleGeoListeningClick(MenuItem menuItem) {
		boolean isChecked = !menuItem.isChecked();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(getString(pref_is_geolistening_enabled), isChecked);
		editor.commit();
		return true;
	}

	/**
	 * This method is called on menu.menu_action_settings click.
	 *
	 * @param menuItem
	 * @return
	 */
	public boolean openSettingsActivity(MenuItem menuItem) {
		startActivity(new Intent(this, SettingsActivity.class));
		return true;
	}

	private void createLocationListener() {
		locationListener = new EventsLocationListener();
		locationListener.setDistanceToAlarm(Settings.DEFAULT_DISTANCE_TO_ALARM);
		locationListener.setNotifier(new Notifier(this));
	}

	/**
	 * Retrieve calendar id in Calendar Provider that contains information about events.
	 * Calendar id is kept in Settings storage. In no calendar had been used (application
	 * started for the first time) new calendar will be created and used (it's id will be returned).
	 *
	 * @return calendar id that contains events.
	 * @throws SecurityException occurs if no calendar is set and
	 *                           permission to create calendars is not granted.
	 */
	private long getCalendarId() throws SecurityException {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		long calendarId = settings.getLong(getString(R.string.pref_calendar_id), EventsSource.DEFAULT_CALENDAR);
		if (calendarId != EventsSource.DEFAULT_CALENDAR) {
			return calendarId;
		}

		LOG.info("No calendar defined in settings. Creating new calendar.");

		calendarId = new CalendarsSource(this).addCalendar();

		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(getString(R.string.pref_calendar_id), calendarId);
		editor.commit();

		return calendarId;
	}

	private void initEventsList() {
		CursorTreeAdapter eventsAdapter = new EventElementAdapter(this);

		ExpandableListView eventsList = (ExpandableListView) findViewById(R.id.events_list);
		eventsList.setAdapter(eventsAdapter);
		eventsList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			private int previouslyExpanded = -1;
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				if (parent.isGroupExpanded(groupPosition)) {
					parent.collapseGroup(groupPosition);
					previouslyExpanded = -1;
				} else {
					if (previouslyExpanded != -1) {
						parent.collapseGroup(previouslyExpanded);
					}
					parent.expandGroup(groupPosition);
					previouslyExpanded = groupPosition;
				}
				return true;
			}
		});
		eventsList.setOnItemLongClickListener((parent, view, position, id) -> {
			long eventId = parent.getAdapter().getItemId(position);
			String eventTitle = ((TextView) view.findViewById(R.id.event_element_title)).getText().toString();

			new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.dialog_delete_event_title)
					.setMessage(MessageFormat.format(getString(R.string.dialog_delete_event_text), eventTitle))
					.setPositiveButton(R.string.dialog_delete_event_yes, (dialog, which) -> {
						MainActivity.this.getEventsSource().remove(eventId);
					})
					.setNegativeButton(R.string.dialog_delete_event_no, null)
					.setCancelable(true)
					.show();
			return true;
		});
	}

	private void initEventsSource() {
		long calendarId = getCalendarId();

		EventsSource eventsSource = new EventsSource(this, calendarId);

		setEventsSource(eventsSource);

		locationListener.setEventsSource(eventsSource);

		ExpandableListView eventsList = (ExpandableListView) findViewById(R.id.events_list);
		CursorTreeAdapter eventsAdapter = (CursorTreeAdapter) eventsList.getExpandableListAdapter();
		getLoaderManager().initLoader(LOADER_EVENTS_ID, null,
				new TasksLoaderCallbacks(this, eventsAdapter, calendarId));
	}

	/**
	 * Enables or disables geo listening based on value stored in shared preferences
	 * (pref_is_geolistening_enabled).
	 *
	 * @throws SecurityException if geolistening cannot be enabled because permission
	 *                           is denied.
	 */
	private void toggleGeoListening() throws SecurityException {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean isEnabled = settings.getBoolean(getString(R.string.pref_is_geolistening_enabled), Settings.DEFAULT_GEO_LISTENING);

		if (locationListener.tryToggle(isEnabled)) {
			locationListener.toggleGeoListening(this);
		}
	}
}
