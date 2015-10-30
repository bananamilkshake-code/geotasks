package me.jtalk.android.geotasks.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import lombok.AllArgsConstructor;
import me.jtalk.android.geotasks.source.EventsSource;

@AllArgsConstructor
public class EventsLocationListener implements LocationListener {
	public static final long MIN_TIME = 0;
	public static final float MIN_DISTANCE = 0;

	private EventsSource eventsSource;

	@Override
	public void onLocationChanged(Location location) {
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
}
