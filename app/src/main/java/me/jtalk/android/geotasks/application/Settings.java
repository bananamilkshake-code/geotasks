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
	public static final Float DEFAULT_DISTANCE_TO_ALARM = 100.0f;

	public static final Long DEFAULT_MIN_TIME = 5L;

	public static final Float DEFAULT_MIN_DISTANCE = 1.0f;
}
