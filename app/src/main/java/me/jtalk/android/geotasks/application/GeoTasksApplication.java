package me.jtalk.android.geotasks.application;

import android.app.Application;
import android.util.Log;

import me.jtalk.android.geotasks.source.EventsSource;

public class GeoTasksApplication extends Application {
	private static final String TAG = GeoTasksApplication.class.getName();

	private EventsSource eventsSource;

	public EventsSource getEventsSource() {
		return eventsSource;
	}

	public void setEventsSource(EventsSource eventsSourceObj) {
		if (this.eventsSource != null) {
			throw new IllegalStateException("EventSource has already being set");
		}

		this.eventsSource = eventsSourceObj;

		Log.d(TAG, "EventsSource is set");
	}
}
