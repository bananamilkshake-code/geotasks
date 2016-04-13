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
package me.jtalk.android.geotasks.application.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.util.Calendar;
import java.util.List;

import lombok.Setter;
import me.jtalk.android.geotasks.application.Settings;
import me.jtalk.android.geotasks.application.Notifier;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;

public class EventsLocationListener implements LocationListener {
	private static final Logger LOG = new Logger(EventsLocationListener.class);

	public static final Long MIN_TIME = Long.valueOf(5);
	public static final Float MIN_DISTANCE = 1.0f;

	private EventsSource eventsSource;

	private Notifier notifier;

	/**
	 * Distance between to event location when reminder must be enabled.
	 */
	@Setter
	private float distanceToAlarm = Settings.DEFAULT_DISTANCE_TO_ALARM;

	public EventsLocationListener(EventsSource eventsSource, Notifier notifier) {
		this.eventsSource = eventsSource;
		this.notifier = notifier;
	}

	@Override
	public void onLocationChanged(Location location) {
		LOG.debug("Location update ({0})", location);

		Calendar currentTime = Calendar.getInstance();
		TaskCoordinates currentCoordinates = new TaskCoordinates(location);
		List<Event> events = eventsSource.getActive(currentTime);

		LOG.debug("Events for check: {0}", events);

		for (Event event : events) {
			double distance = event.getCoordinates().distanceTo(currentCoordinates);
			LOG.debug("Event {0} is checked. Distance {1}", event.getTitle(), distance);
			if (distance <= distanceToAlarm) {
				LOG.debug("Notify about event {0} (distance {1}, current coordinates {2})",
						event.getTitle(), distance, currentCoordinates);
				notifier.onEventIsNear(event, currentCoordinates, distance);
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}
}
