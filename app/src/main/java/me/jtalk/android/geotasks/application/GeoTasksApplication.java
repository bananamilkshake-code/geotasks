package me.jtalk.android.geotasks.application;

import android.app.Application;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.jtalk.android.geotasks.source.EventsSource;

public class GeoTasksApplication extends Application {
	private static final Logger LOG = LoggerFactory.getLogger(GeoTasksApplication.class);

	private EventsSource eventsSource;

	public EventsSource getEventsSource() {
		return eventsSource;
	}

	public void setEventsSource(EventsSource eventsSourceObj) {
		if (this.eventsSource != null) {
			throw new IllegalStateException("EventSource has already being set");
		}

		this.eventsSource = eventsSourceObj;

		LOG.debug("EventsSource is set");
	}
}
