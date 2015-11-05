package me.jtalk.android.geotasks.location;

import android.location.Location;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class TaskCoordinates {
	@Getter
	@Setter
	private double latitude;

	@Getter
	@Setter
	private double longitude;

	public TaskCoordinates(Location location) {
		this(location.getLatitude(), location.getLongitude());
	}

	public TaskCoordinates(IGeoPoint geoPoint) {
		this(geoPoint.getLatitude(), geoPoint.getLongitude());
	}

	public int distanceTo(TaskCoordinates taskCoordinates) {
		return toGeoPoint().distanceTo(taskCoordinates.toGeoPoint());
	}

	public GeoPoint toGeoPoint() {
		return new GeoPoint(getLatitude(), getLongitude());
	}
}
