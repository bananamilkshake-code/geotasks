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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.text.MessageFormat;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.Notifier;
import me.jtalk.android.geotasks.application.Settings;
import me.jtalk.android.geotasks.application.listeners.EventsLocationListener;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;

public class LocationTrackService extends Service {

	private static final Logger LOG = new Logger(LocationTrackService.class);

	private LocationBinder binder = new LocationBinder();

	private LocationManager getLocationManager() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind (Intent intent) {
		LOG.debug("Service unbind");

		Toast.makeText(this, "LocationTrackService disabled", Toast.LENGTH_SHORT).show();

		return super.onUnbind(intent);
	}

	public class LocationBinder extends Binder implements SharedPreferences.OnSharedPreferenceChangeListener {
		// TODO: make list of locationListeners because LocationTrackService always
		// returns first created IBinder object.
		private EventsLocationListener locationListener;

		public void setup(EventsSource eventsSource, Notifier notifier) throws SecurityException {
			locationListener = new EventsLocationListener(eventsSource, notifier);

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			updateDistanceToAlarm(settings);
			setupLocationListenerUpdateParameters(settings);

			settings.registerOnSharedPreferenceChangeListener(this);
		}

		public void disable() throws SecurityException {
			LocationTrackService.this.getLocationManager().removeUpdates(locationListener);

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			settings.unregisterOnSharedPreferenceChangeListener(this);

			locationListener = null;

			LOG.debug("Geo listening disabled");
		}

		private void updateDistanceToAlarm(SharedPreferences settings) {
			float distanceToAlarm = Float.parseFloat(settings.getString(getString(R.string.pref_alarm_distance), Settings.DEFAULT_DISTANCE_TO_ALARM.toString()));
			locationListener.setDistanceToAlarm(distanceToAlarm);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) throws SecurityException {
			if (key.equals(getString(R.string.pref_alarm_distance))) {
				updateDistanceToAlarm(sharedPreferences);
			} if (key.equals(getString(R.string.pref_location_update_min_distance)) || key.equals(getString(R.string.pref_location_update_min_time))) {
				setupLocationListenerUpdateParameters(sharedPreferences);
				LocationTrackService.this.getLocationManager().removeUpdates(locationListener);
			}
		}

		private void setupLocationListenerUpdateParameters(SharedPreferences sharedPreferences) throws SecurityException {
			LocationManager locationManager = LocationTrackService.this.getLocationManager();

			float minDistance = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_location_update_min_distance), EventsLocationListener.MIN_DISTANCE.toString()));
			long minTime = Long.parseLong(sharedPreferences.getString(getString(R.string.pref_location_update_min_time), EventsLocationListener.MIN_TIME.toString()));

			String provider = LocationManager.PASSIVE_PROVIDER;
			if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
				provider = LocationManager.NETWORK_PROVIDER;
			} else if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
				provider = LocationManager.GPS_PROVIDER;
			}

			locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener);

			LOG.debug("Geo listening enabled via {0}", provider);

			Toast.makeText(LocationTrackService.this, MessageFormat.format("Geo listening enabled via {0}", provider), Toast.LENGTH_SHORT).show();
		}
	}
}
