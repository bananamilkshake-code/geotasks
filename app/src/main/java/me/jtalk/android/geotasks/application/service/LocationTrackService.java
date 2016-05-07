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
package me.jtalk.android.geotasks.application.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import lombok.Setter;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.activity.MainActivity;
import me.jtalk.android.geotasks.application.Settings;
import me.jtalk.android.geotasks.application.receiver.NotificationReceiver;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventIntentFields;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;

public class LocationTrackService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener, EventIntentFields, LocationListener {

	private static final Logger LOG = new Logger(LocationTrackService.class);

	private static final int STATUS_BAR_NOTIFICATION_ID = Integer.MAX_VALUE;

	private LocationManager getLocationManager() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		if (intent == null) {
			stopSelf();
			return START_NOT_STICKY;
		}

		long calendarId = intent.getLongExtra(INTENT_EXTRA_CALENDAR_ID, EventsSource.DEFAULT_CALENDAR);
		if (calendarId == EventsSource.DEFAULT_CALENDAR) {
			throw new IllegalArgumentException("Calendar id for service is incorrect or not passed");
		}

		setupListener(calendarId);
		createNotification();

		return START_STICKY;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) throws SecurityException {
		if (key.equals(getString(R.string.pref_alarm_distance))) {
			updateDistanceToAlarm(sharedPreferences);
		} if (key.equals(getString(R.string.pref_location_update_min_distance)) || key.equals(getString(R.string.pref_location_update_min_time))) {
			setupLocationListenerUpdateParameters(sharedPreferences);
			getLocationManager().removeUpdates(this);
		}
	}

	private EventsSource eventsSource;

	/**
	 * Distance between to event location when reminder must be enabled.
	 */
	@Setter
	private float distanceToAlarm = Settings.DEFAULT_DISTANCE_TO_ALARM;

	@Override
	public void onLocationChanged(Location location) {
		LOG.debug("Location update ({0})", location);

		Calendar currentTime = Calendar.getInstance();
		TaskCoordinates currentCoordinates = new TaskCoordinates(location);
		List<Event> events = eventsSource.getActiveLocationEvents(currentTime);

		LOG.debug("Events for check: {0}", events);

		for (Event event : events) {
			double distance = event.getCoordinates().distanceTo(currentCoordinates);
			LOG.debug("Event {0} is checked. Distance {1}", event.getTitle(), distance);
			if (distance <= distanceToAlarm) {
				LOG.debug("Notify about event {0} (distance {1}, current coordinates {2})",
						event.getTitle(), distance, currentCoordinates);
				sendBroadcast(createIntent(eventsSource.getCalendarId(), event.getId(), currentCoordinates, distance));
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	private Intent createIntent(long calendarId, long eventId, TaskCoordinates currentPosition, double distance) {
		Intent intent = new Intent(NotificationReceiver.ACTION_ALARM);
		intent.putExtra(NotificationReceiver.INTENT_EXTRA_CALENDAR_ID, calendarId);
		intent.putExtra(NotificationReceiver.INTENT_EXTRA_EVENT_ID, eventId);
		intent.putExtra(NotificationReceiver.INTENT_EXTRA_CURRENT_POSITION, currentPosition);
		intent.putExtra(NotificationReceiver.INTENT_EXTRA_DISTANCE, distance);
		return intent;
	}

	/**
	 * Creates and shows notification in status bar for application.
	 */
	private void createNotification() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		Notification.Builder builder = new Notification.Builder(this)
				.setContentTitle(getText(R.string.app_name))
				.setAutoCancel(false)
				.setOngoing(true)
				.setSmallIcon(R.drawable.treasure_map_white_24)
				.setContentIntent(pendingIntent);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			builder.setLargeIcon(getLargeIcon());
		}

		startForeground(STATUS_BAR_NOTIFICATION_ID, builder.build());
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Icon getLargeIcon() {
		return Icon.createWithResource(this, R.drawable.treasure_map_colour_50);
	}

	private void setupListener(long calendarId) throws SecurityException {
		getLocationManager().removeUpdates(this);

		eventsSource = new EventsSource(this, calendarId);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		setupLocationListenerUpdateParameters(settings);
	}

	private void updateDistanceToAlarm(SharedPreferences settings) {
		float distanceToAlarm = Float.parseFloat(settings.getString(getString(R.string.pref_alarm_distance), Settings.DEFAULT_DISTANCE_TO_ALARM.toString()));
		setDistanceToAlarm(distanceToAlarm);
	}

	private void setupLocationListenerUpdateParameters(SharedPreferences sharedPreferences) {
		LocationManager locationManager = getLocationManager();

		float minDistance = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_location_update_min_distance), Settings.DEFAULT_MIN_DISTANCE.toString()));
		long minTime = Long.parseLong(sharedPreferences.getString(getString(R.string.pref_location_update_min_time), Settings.DEFAULT_MIN_TIME.toString()));

		String provider = LocationManager.PASSIVE_PROVIDER;
		if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		} else if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		}

		try {
			locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
			LOG.info("Geo listening enabled via {0}", provider);
		} catch (SecurityException exception) {
			Toast.makeText(this, R.string.location_service_toast_no_permission_errror, Toast.LENGTH_LONG).show();
			LOG.error("Geo listening cannot be enabled due o lack of permission: {0}", exception.getMessage());
			stopSelf();
		}
	}
}
