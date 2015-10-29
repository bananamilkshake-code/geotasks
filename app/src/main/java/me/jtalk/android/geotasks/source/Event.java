package me.jtalk.android.geotasks.source;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Event {
	private String title;
	private String startTime;

	public String getTitle() {
		return title;
	}

	public String getStartTimeText() {
		return startTime;
	}
}
