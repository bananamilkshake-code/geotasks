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
package me.jtalk.android.geotasks.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.acra.ACRA;

import java.text.MessageFormat;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.Notifier;
import me.jtalk.android.geotasks.application.Settings;
import me.jtalk.android.geotasks.activity.item.EventElementAdapter;
import me.jtalk.android.geotasks.application.callbacks.TasksLoaderCallbacks;
import me.jtalk.android.geotasks.application.service.LocationTrackService;
import me.jtalk.android.geotasks.source.CalendarsSource;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;
import me.jtalk.android.geotasks.util.PermissionDependentTask;
import me.jtalk.android.geotasks.util.TasksChain;

import static me.jtalk.android.geotasks.R.string.pref_is_geolistening_enabled;

public class MainActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final Logger LOG = new Logger(MainActivity.class);

	private static final int LOADER_EVENTS_ID = 0;

	private int initChainId;
	private int toggleGeoListenChainId;

	private MenuItem geoTrackMenuItem;

	private LocationTrackServiceConnection locationTrackServiceConnection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initChainId = addTaskChain(new TasksChain<PermissionDependentTask>()
				.add(makeTask(this::getCalendarId, Manifest.permission.WRITE_CALENDAR))
				.add(makeTask(this::initEventsList, Manifest.permission.READ_CALENDAR))
				.add(makeTask(this::initEventsSource, Manifest.permission.READ_CALENDAR)));

		toggleGeoListenChainId = addTaskChain(new TasksChain<PermissionDependentTask>()
				.add(makeTask(this::setupGeoListening, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)));

		processChain(initChainId);

 		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		this.geoTrackMenuItem = menu.findItem(R.id.menu_action_enable_geolistening);
		processChain(toggleGeoListenChainId);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] values) {
		processPermissionRequestResult(requestCode, permissions, values);
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
			processChain(toggleGeoListenChainId);
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

		LOG.debug("Toggling geolistening: make checked {0}", isChecked);

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

	public boolean sendReports(MenuItem menuItem) {
		ACRA.getErrorReporter().handleSilentException(new Exception());
		return true;
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
			private static final int NOTHING_EXPANDED = -1;

			private int previouslyExpanded = NOTHING_EXPANDED;

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				if (parent.isGroupExpanded(groupPosition)) {
					parent.collapseGroup(groupPosition);
					previouslyExpanded = NOTHING_EXPANDED;
				} else {
					if (previouslyExpanded != NOTHING_EXPANDED) {
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

		setEventsSource(new EventsSource(this, calendarId));

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
	private void setupGeoListening() throws SecurityException {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean isEnabled = settings.getBoolean(getString(R.string.pref_is_geolistening_enabled), Settings.DEFAULT_GEO_LISTENING);

		if (isEnabled) {
			LOG.debug("Bind to LocationTrackService");
			locationTrackServiceConnection = new LocationTrackServiceConnection();
			if (!bindService(new Intent(this, LocationTrackService.class), locationTrackServiceConnection, BIND_AUTO_CREATE)) {
				LOG.error("Failed to bind LocationTrackService");
			}

			geoTrackMenuItem.setChecked(true);
			geoTrackMenuItem.setIcon(R.drawable.ic_gps_fixed_black_48dp);

			Toast.makeText(this, R.string.toast_geolistening_enabled, Toast.LENGTH_SHORT).show();
		} else {
			if (locationTrackServiceConnection != null) {
				LOG.debug("Unbinding from LocationTrackService");
				locationTrackServiceConnection.locationBinder.disable();
				unbindService(locationTrackServiceConnection);
			}

			geoTrackMenuItem.setChecked(false);
			geoTrackMenuItem.setIcon(R.drawable.ic_gps_off_black_48dp);

			Toast.makeText(this, R.string.toast_geolistening_disnabled, Toast.LENGTH_SHORT).show();
		}
	}

	private class LocationTrackServiceConnection implements ServiceConnection {

		public LocationTrackService.LocationBinder locationBinder;

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			locationBinder = (LocationTrackService.LocationBinder) service;
			locationBinder.setup(MainActivity.this.getEventsSource(), new Notifier(MainActivity.this), Settings.DEFAULT_DISTANCE_TO_ALARM);

			LOG.debug("LocationTrackService is connected");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			LOG.debug("LocationTrackService is disconnected");
		}
	}
}
