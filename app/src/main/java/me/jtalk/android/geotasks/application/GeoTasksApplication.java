package me.jtalk.android.geotasks.application;

import android.app.Application;
import android.content.Context;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import me.jtalk.android.geotasks.BuildConfig;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.listeners.CrashReportsStatusChangeListener;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;

@ReportsCrashes(
		formUri = "http://192.168.1.127:5000/reports",
		buildConfigClass = BuildConfig.class,
		mode = ReportingInteractionMode.DIALOG,
		resDialogTitle = R.string.crash_dialog_title,
		resDialogText = R.string.crash_dialog_text,
		resDialogOkToast = R.string.crash_dialog_ok_toast,
		resDialogPositiveButtonText = R.string.crash_positive_button,
		sharedPreferencesMode = Context.MODE_PRIVATE
)
public class GeoTasksApplication extends Application {
	private static final Logger LOG = new Logger(GeoTasksApplication.class);

	private EventsSource eventsSource;

	private CrashReportsStatusChangeListener crashReportsStatusChangeListener = new CrashReportsStatusChangeListener(this);

	@Override
	public void onCreate() {
		super.onCreate();
		
		AndroidGraphicFactory.createInstance(this);

		ACRA.init(this);

		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.registerOnSharedPreferenceChangeListener(crashReportsStatusChangeListener);
	}

	@Override
	public void onTerminate() {
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.unregisterOnSharedPreferenceChangeListener(crashReportsStatusChangeListener);

		super.onTerminate();
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
