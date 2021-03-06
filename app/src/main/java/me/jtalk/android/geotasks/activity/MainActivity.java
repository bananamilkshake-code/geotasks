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

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.acra.ACRA;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.activity.item.EventElementAdapter;
import me.jtalk.android.geotasks.application.callbacks.TasksLoaderCallbacks;
import me.jtalk.android.geotasks.application.receiver.EventChangedReceiver;
import me.jtalk.android.geotasks.application.service.LocationTrackService;
import me.jtalk.android.geotasks.application.service.Permission;
import me.jtalk.android.geotasks.application.service.PermissionAwareRunner;
import me.jtalk.android.geotasks.source.CalendarsSource;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.CursorHelper;
import me.jtalk.android.geotasks.util.Logger;

public class MainActivity extends BaseActivity {

	private static final Logger LOG = new Logger(MainActivity.class);

	private static final int LOADER_EVENTS_ID = 0;

	private MenuItem geoTrackMenuItem;

	private final PermissionAwareRunner permissionAwareRunner = new PermissionAwareRunner(this, this::showPermissionAlert);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initCalendar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		getMenuInflater().inflate(R.menu.main, menu);
		this.geoTrackMenuItem = menu.findItem(R.id.menu_action_enable_geolistening);
		if (preferences.getBoolean(getString(R.string.pref_is_geolistening_enabled), false)) {
			permissionAwareRunner.withPermissions(Permission.TRACK_LOCATION, this::enableGeoListening);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] values) {
		permissionAwareRunner.onPermissionUpdate(permissions, values, requestCode);
	}

	/**
	 * This method is called on menu.menu_action_enable_geolistening click.
	 */
	public void openAddEventIntent(MenuItem menuItem) {
		startActivity(new Intent(this, MakeTaskActivity.class));
	}

	/**
	 * This method is called on menu.menu_action_add_event click.
	 */
	public void toggleGeoListeningClick(MenuItem menuItem) {
		boolean isChecked = menuItem.isChecked();
		if (isChecked) {
			LOG.debug("Disabling GeoListening");
			disableGeoListening();
		} else {
			LOG.debug("Enabling GeoListening");
			permissionAwareRunner.withPermissions(Permission.TRACK_LOCATION, this::enableGeoListening);
		}
	}

	/**
	 * This method is called on menu.menu_action_settings click.
	 */
	public void openSettingsActivity(MenuItem menuItem) {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public boolean sendReports(MenuItem menuItem) {
		ACRA.getErrorReporter().handleSilentException(new Exception());
		return true;
	}

	private void showPermissionAlert(Permission failedPermission) {
		switch (failedPermission) {
			case MANAGE_CALENDAR:
				new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.main_dialog_no_permission_for_calendar_creation_title)
						.setMessage(R.string.main_dialog_no_permission_for_calendar_creation_message)
						.setPositiveButton(R.string.main_dialog_no_permission_for_calendar_creation_retry_button, (dialog, which) -> {
							dialog.dismiss();
							initCalendar();
						})
						.setNegativeButton(R.string.main_dialog_no_permission_for_calendar_creation_close_button, (dialog, which) -> {
							dialog.dismiss();
							MainActivity.this.finish();
						})
						.show();
				break;
			default:
				Toast.makeText(this, failedPermission.getErrorMessageId(), Toast.LENGTH_LONG)
						.show();
				break;
		}
	}

	private void initCalendar() {
		permissionAwareRunner.withPermissions(Permission.MANAGE_CALENDAR, () -> {
			initEventsSource();
			initEventsList();
		});
	}

	/**
	 * Retrieve calendar id in Calendar Provider that contains information about events.
	 * Calendar id is kept in Settings storage. If no calendar had been used (application
	 * started for the first time) new calendar will be created and used (it's id will be returned).
	 *
	 * @throws SecurityException occurs if no calendar is set and
	 *                           permission to create calendars is not granted.
	 */
	private void initEventsSource() throws SecurityException {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		long calendarId = settings.getLong(getString(R.string.pref_calendar_id), EventsSource.DEFAULT_CALENDAR);
		if (calendarId == EventsSource.DEFAULT_CALENDAR) {
			calendarId = new CalendarsSource(this).add();

			LOG.info("No calendar was defined in settings. Created calendar {0}", calendarId);

			SharedPreferences.Editor editor = settings.edit();
			editor.putLong(getString(R.string.pref_calendar_id), calendarId);
			editor.apply();
		}

		setEventsSource(new EventsSource(this, calendarId));
		setupEventsAlarms();
	}

	private void initEventsList() {
		CursorTreeAdapter eventsAdapter = new EventElementAdapter(this);

		ExpandableListView eventsList = (ExpandableListView) findViewById(R.id.main_events_list);
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
			Event event = CursorHelper.extractEvent(eventsAdapter.getGroup(position));
			new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.main_dialog_delete_event_title)
					.setMessage(MessageFormat.format(getString(R.string.main_dialog_delete_event_text), event.getTitle()))
					.setPositiveButton(R.string.main_dialog_delete_event_yes, (d, w) -> getEventsSource().remove(event.getId()))
					.setNegativeButton(R.string.main_dialog_delete_event_no, null)
					.setCancelable(true)
					.show();
			return true;
		});

		getLoaderManager().initLoader(LOADER_EVENTS_ID, null,
				new TasksLoaderCallbacks(this, eventsAdapter, getEventsSource().getCalendarId()));
	}

	private void enableGeoListening() throws SecurityException {
		Intent intent = new Intent(this, LocationTrackService.class);
		intent.putExtra(LocationTrackService.INTENT_EXTRA_CALENDAR_ID, getEventsSource().getCalendarId());
		startService(intent);
		geoTrackMenuItem.setChecked(true);
		geoTrackMenuItem.setIcon(R.drawable.ic_gps_fixed_white_48dp);
		PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext())
				.edit()
				.putBoolean(getString(R.string.pref_is_geolistening_enabled), true)
				.apply();
	}

	private void disableGeoListening() throws SecurityException {
		Intent intent = new Intent(this, LocationTrackService.class);
		stopService(intent);
		geoTrackMenuItem.setChecked(false);
		geoTrackMenuItem.setIcon(R.drawable.ic_gps_off_white_48dp);
		PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext())
				.edit()
				.putBoolean(getString(R.string.pref_is_geolistening_enabled), false)
				.apply();
	}

	private void setupEventsAlarms() {
		LOG.debug("Setup previous events alarms");
		EventChangedReceiver eventChangesReceiver = new EventChangedReceiver();
		List<Event> events = getEventsSource().getActiveTimingEvents(Calendar.getInstance());
		for (Event event : events) {
			eventChangesReceiver.cancelAlarm(this, getEventsSource().getCalendarId(), event.getId());
			eventChangesReceiver.setupAlarm(this, getEventsSource().getCalendarId(), event.getId());
		}
		registerReceiver(eventChangesReceiver, new IntentFilter(EventsSource.ACTION_EVENT_CHANGED));
	}
}
