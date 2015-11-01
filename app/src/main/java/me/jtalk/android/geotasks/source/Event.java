package me.jtalk.android.geotasks.source;

import org.osmdroid.api.IGeoPoint;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Event {
	@Getter
	private String title;

	@Getter
	private String startTimeText;

	@Getter
	private IGeoPoint geoPoint;
}
