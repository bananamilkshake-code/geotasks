package me.jtalk.android.geotasks.application;

import android.app.Application;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;

public class GeoTasksApplication extends Application {
	private static final Logger LOG = new Logger(GeoTasksApplication.class);

	private EventsSource eventsSource;

	@Override
	public void onCreate() {
		super.onCreate();
		AndroidGraphicFactory.createInstance(this);
	}

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
