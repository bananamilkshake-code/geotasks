package me.jtalk.android.geotasks.application.listeners;

import android.view.GestureDetector;
import android.view.MotionEvent;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.util.MapViewProjection;

import lombok.AllArgsConstructor;
import me.jtalk.android.geotasks.activity.LocationPickActivity;
import me.jtalk.android.geotasks.location.TaskCoordinates;

/**
 * Mark on the map must be placed if only single tap has occurred. OnTouchEvent doesn't provide
 * convenient way to detect if touch was single, that's why implementation of GestureListener is
 * needed: GestureDetector can identify click type more precisely and single tap can be catched
 * easily.
 */
@AllArgsConstructor
public class MapGestureDetector extends GestureDetector.SimpleOnGestureListener {
	private LocationPickActivity locationPickActivity;

	@Override
	public boolean onSingleTapConfirmed(MotionEvent event) {
		LatLong pickedLatLong = new MapViewProjection(locationPickActivity.getMapView()).fromPixels(event.getX(), event.getY());
		locationPickActivity.onLocationPick(new TaskCoordinates(pickedLatLong));
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent event1, MotionEvent event2, final float distanceX, float distanceY) {
		return super.onScroll(event1, event2, distanceX, distanceY);
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
		return super.onFling(event1, event2, velocityX, velocityY);
	}
}