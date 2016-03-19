package me.jtalk.android.geotasks.application.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import me.jtalk.android.geotasks.application.Notifier;
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

		return super.onUnbind(intent);
	}

	public class LocationBinder extends Binder {
		// TODO: make list of locationListeners because LocationTrackService always
		// returns first created IBinder object.
		private EventsLocationListener locationListener;

		public void setup(EventsSource eventsSource, Notifier notifier, float distanceToAlarm) throws SecurityException {
			locationListener = new EventsLocationListener(eventsSource, notifier);
			locationListener.setDistanceToAlarm(distanceToAlarm);

			LocationManager locationManager = LocationTrackService.this.getLocationManager();
			if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						EventsLocationListener.MIN_TIME,
						EventsLocationListener.MIN_DISTANCE,
						locationListener);

				LOG.debug("Geo listening enabled via NetworkProvider");
			}

			if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER,
						EventsLocationListener.MIN_TIME,
						EventsLocationListener.MIN_DISTANCE,
						locationListener);

				LOG.debug("Geo listening enabled via GpsProvider");
			}
		}

		public void disable() throws SecurityException {
			LocationTrackService.this.getLocationManager().removeUpdates(locationListener);

			LOG.debug("Geo listening disabled");
		}
	}
}
