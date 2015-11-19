package me.jtalk.android.geotasks.application.listeners;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.util.Logger;

/**
 * We want to let user choose crash reports send status: user must be able
 * to simply enable or disable crash report sending or let application
 * ask about sending report every crash.
 *
 * ACRA can use 2 shared preferences: acra.disabled (disable sending reports) and
 * acra.alwaysaccept (always silently send reports). We can bound this preferences
 * to make ACRA work as we need.
 */
@AllArgsConstructor
public class CrashReportsStatusChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final Logger LOG = new Logger(CrashReportsStatusChangeListener.class);

	private static final Map<String, Map<String, Boolean>> ACRA_PREFERENCES;

	private static final String PREF_ACRA_DISABLED = "acra.disable";
	private static final String PREF_ACRA_ALWAYSACCEPT = "acra.alwaysaccept";

	private static final String STATUS_ASK = "Ask";
	private static final String STATUS_ENABLE = "Enable";
	private static final String STATUS_DISABLE = "Disable";

	private static final String DEFAULT_STATUS = STATUS_ASK;

	private static void addPrefMapping(String prefValue, boolean isDisabled, boolean isAlwaysAccept) {
		Map<String, Boolean> prefMapping = new HashMap<>();
		prefMapping.put(PREF_ACRA_DISABLED, isDisabled);
		prefMapping.put(PREF_ACRA_ALWAYSACCEPT, isAlwaysAccept);
		ACRA_PREFERENCES.put(prefValue, prefMapping);
	}

	static {
		ACRA_PREFERENCES = new HashMap<>();
		addPrefMapping(STATUS_ASK, false, false);
		addPrefMapping(STATUS_ENABLE, false, true);
		addPrefMapping(STATUS_DISABLE, true, true);
	}

	@NonNull
	private final Context context;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key != context.getString(R.string.pref_crash_report_send_status)) {
			return;
		}

		String newStatus = sharedPreferences.getString(key, DEFAULT_STATUS);

		LOG.debug("Crash report status changed to {}", newStatus);

		Map<String, Boolean> mappedPrefs = ACRA_PREFERENCES.get(newStatus);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		for (Map.Entry<String, Boolean> entry : mappedPrefs.entrySet()) {
			editor.putBoolean(entry.getKey(), entry.getValue());
		}

		editor.commit();
	}
}
