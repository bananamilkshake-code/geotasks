package me.jtalk.android.geotasks.application;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import me.jtalk.android.geotasks.BuildConfig;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;

@ReportsCrashes(
		formUri = "http://192.168.1.127:5000/reports",
		buildConfigClass = BuildConfig.class
)
public class GeoTasksApplication extends Application {
	private static final Logger LOG = new Logger(GeoTasksApplication.class);

	private EventsSource eventsSource;

	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(this);
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
