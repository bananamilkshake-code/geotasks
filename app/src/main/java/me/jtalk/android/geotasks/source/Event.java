package me.jtalk.android.geotasks.source;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.jtalk.android.geotasks.util.GeoPointFormat;

@AllArgsConstructor
public class Event {
	@Getter
	private String title;

	@Getter
	private String startTimeText;

	@Getter
	private IGeoPoint geoPoint;

	public String getLocationText() {
		if (geoPoint == null) {
			return null;
		}

		return GeoPointFormat.format(geoPoint);
	}
}
