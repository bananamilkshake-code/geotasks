package me.jtalk.android.geotasks.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.NoArgsConstructor;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.source.EventsSource;

@NoArgsConstructor
public class EventsLocationListener implements LocationListener {
	private static final Logger LOG = LoggerFactory.getLogger(EventsLocationListener.class);

	public static final long MIN_TIME = 0;
	public static final float MIN_DISTANCE = 0;

	private EventsSource eventsSource;

	/**
	 * MenuItem that toggles geo location listening.
	 */
	private MenuItem menuItem;

	private boolean isEnabled = false;

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

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public void toggleGeoListening(Context context) throws SecurityException {
		menuItem.setChecked(isEnabled);

		if (isEnabled) {
			onEnabled(context);
		} else {
			onDisabled(context);
		}
	}

	private void onEnabled(Context context) throws SecurityException {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				EventsLocationListener.MIN_TIME,
				EventsLocationListener.MIN_DISTANCE,
				this);

		menuItem.setIcon(R.drawable.ic_gps_fixed_black_48dp);

		Toast.makeText(context, R.string.toast_geolistening_enabled, Toast.LENGTH_SHORT).show();

		LOG.debug("Geo listening enabled");
	}

	private void onDisabled(Context context)  throws SecurityException {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);

		menuItem.setIcon(R.drawable.ic_gps_off_black_48dp);

		Toast.makeText(context, R.string.toast_geolistening_disnabled, Toast.LENGTH_SHORT).show();

		LOG.debug("Geo listening disabled");
	}

	public void setEventsSource(EventsSource eventsSource) {
		this.eventsSource = eventsSource;
	}

	public boolean tryToggle(boolean enabled) {
		if (this.isEnabled == enabled) {
			return false;
		}

		this.isEnabled = enabled;
		return true;
	}
}
