package me.jtalk.android.geotasks.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.Settings;
import me.jtalk.android.geotasks.activity.item.EventElementAdapter;
import me.jtalk.android.geotasks.location.EventsLocationListener;
import me.jtalk.android.geotasks.source.CalendarsSource;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.PermissionDependentTask;
import me.jtalk.android.geotasks.util.TasksChain;

public class MainActivity extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

	private static final int LOADER_EVENTS_ID = 0;

	private TasksChain<PermissionDependentTask> initChain;

	private LocationListener locationListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initChain = new TasksChain<PermissionDependentTask>()
				.add(makeTask(this::getCalendarId, Manifest.permission.WRITE_CALENDAR))
				.add(makeTask(this::initEventsList, Manifest.permission.READ_CALENDAR))
				.add(makeTask(this::initEventsSource, Manifest.permission.READ_CALENDAR));

		processChain(initChain);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		boolean isGeoListeningEnabled = settings.getBoolean(Settings.GEO_LISTENING, Settings.DEFAULT_GEO_LISTENING);
		toggleGeoListening(menu.findItem(R.id.menu_action_enable_geolistening), isGeoListeningEnabled);

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

	/**
	 * This method is called on menu.menu_action_enable_geolistening click.
	 *
	 * @param menuItem
	 * @return
	 */
	public boolean openAddEventIntent(MenuItem menuItem) {
		startActivity(new Intent(this, AddEventActivity.class));
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

		toggleGeoListening(menuItem, isChecked);

		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(Settings.GEO_LISTENING, isChecked);
		editor.commit();
		return true;
	}

	/**
	 * Retrieve calendar id in Calendar Provider that contains information about events.
	 * Calendar id is kept in Settings storage. In no calendar had been used (application
	 * started for the first time) new calendar will be created and used (it's id will be returned).
	 *
	 * @return calendar id that contains events.
	 * @throws SecurityException occurs if no calendar is set and
	 * permission to create calendars is not granted.
	 */
	private long getCalendarId() throws SecurityException {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);

		long calendarId = settings.getLong(Settings.CALENDAR_ID, EventsSource.DEFAULT_CALENDAR);
		if (calendarId != EventsSource.DEFAULT_CALENDAR) {
			return calendarId;
		}

		LOG.info("No calendar defined in settings. Creating new calendar.");

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

		locationListener = new EventsLocationListener(eventsSource);
	}

	private void toggleGeoListening(MenuItem menuItem, boolean isChecked) throws SecurityException {
		menuItem.setChecked(isChecked);

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (isChecked) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					EventsLocationListener.MIN_TIME,
					EventsLocationListener.MIN_DISTANCE,
					locationListener);

			menuItem.setIcon(R.drawable.ic_gps_fixed_black_48dp);

			Toast.makeText(this, R.string.toast_geolistening_enabled, Toast.LENGTH_SHORT).show();
		} else {
			locationManager.removeUpdates(locationListener);

			menuItem.setIcon(R.drawable.ic_gps_off_black_48dp);

			Toast.makeText(this, R.string.toast_geolistening_disnabled, Toast.LENGTH_SHORT).show();
		}
	}
}
