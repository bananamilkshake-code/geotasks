package me.jtalk.android.geotasks.source;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.jtalk.android.geotasks.util.GeoPointFormat;

@AllArgsConstructor()
public class Event {
	@Getter
	private long id;

	@Getter
	private String title;

	@Getter
	private String startTimeText;

	@Getter
	private GeoPoint geoPoint;

	public String getLocationText() {
		if (geoPoint == null) {
			return null;
		}

		return GeoPointFormat.format(geoPoint);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof Event) {
			return this.id == ((Event) obj).getId();
		}

		return false;
	}
}
