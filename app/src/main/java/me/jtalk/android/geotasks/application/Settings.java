package me.jtalk.android.geotasks.application;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Keeps name of fields and default values in application Settings storage.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Settings {
	public static final boolean DEFAULT_GEO_LISTENING = false;

	/**
	 * Default value of distance to event location when reminder must be enabled.
	 */
	public static final float DEFAULT_DISTANCE_TO_ALARM = 100;
}
