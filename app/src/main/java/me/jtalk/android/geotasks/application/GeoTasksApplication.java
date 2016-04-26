/*
 * Copyright (C) 2016 Liza Lukicheva
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package me.jtalk.android.geotasks.application;

import android.app.Application;
import android.content.Context;

import org.acra.sender.HttpSender;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import lombok.Getter;
import lombok.Setter;
import me.jtalk.android.geotasks.BuildConfig;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.listeners.CrashReportsStatusChangeListener;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;

@ReportsCrashes(
		formUri = "https://as.jtalk.me/jacra-submit/report",
		buildConfigClass = BuildConfig.class,
		mode = ReportingInteractionMode.DIALOG,
		resDialogTitle = R.string.crash_dialog_title,
		resDialogText = R.string.crash_dialog_text,
		resDialogOkToast = R.string.crash_dialog_ok_toast,
		resDialogPositiveButtonText = R.string.crash_positive_button,
		sharedPreferencesMode = Context.MODE_PRIVATE,
		reportType = HttpSender.Type.JSON,
		formUriBasicAuthLogin = BuildConfig.JACRA_LOGIN,
		formUriBasicAuthPassword = BuildConfig.JACRA_PASSWORD
)
public class GeoTasksApplication extends Application {
	private static final Logger LOG = new Logger(GeoTasksApplication.class);

	@Setter
	@Getter
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
}
