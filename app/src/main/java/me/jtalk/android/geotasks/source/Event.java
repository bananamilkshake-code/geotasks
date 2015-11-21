package me.jtalk.android.geotasks.source;

import java.util.Calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.util.CoordinatesFormat;

@AllArgsConstructor()
public class Event {
	@Getter
	private long id;

	@Getter
	private String title;

	@Getter
	private Calendar startTime;

	@Getter
	private TaskCoordinates coordinates;

	public String getLocationText() {
		if (coordinates == null) {
			return null;
		}

		return CoordinatesFormat.format(coordinates);
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
