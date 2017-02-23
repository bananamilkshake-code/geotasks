package me.jtalk.android.geotasks.util;


import android.content.SharedPreferences;

import com.google.common.base.Objects;

public class PreferencesHelper {

    public static boolean isEnabled(SharedPreferences preferences, String name) {
        return isEnabled(preferences, name, false);
    }

    public static boolean isEnabled(SharedPreferences preferences, String name, boolean defaultEnabled) {
        return preferences.getBoolean(name, defaultEnabled);
    }

    public static boolean isEnabledWithUpdate(SharedPreferences preferences, String desiredKey, String actualKey) {
        if (!Objects.equal(desiredKey, actualKey)) {
            return false;
        }
        return isEnabled(preferences, actualKey);
    }
}
