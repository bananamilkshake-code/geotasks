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
package me.jtalk.android.geotasks.application.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.jtalk.android.geotasks.application.Notifier;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;

/**
 * This class will catch events that must create notifications about
 * theirs occurrence.
 */
public class NotificationReceiver extends BroadcastReceiver {

	private static final Logger LOG = new Logger(NotificationReceiver.class);

	public static final String ACTION_ALARM = "me.jtalk.geotasks.NOTIFY_EVENT_ALARM";

	public static final String INTENT_EXTRA_CALENDAR_ID = "calendar-id";
	public static final String INTENT_EXTRA_EVENT_ID = "event-id";

	@Override
	public void onReceive(Context context, Intent intent) {
		String acton = intent.getAction();
		long calendarId = intent.getLongExtra(INTENT_EXTRA_CALENDAR_ID, EventsSource.DEFAULT_CALENDAR);
		long eventId = intent.getLongExtra(INTENT_EXTRA_EVENT_ID, EventsSource.NO_TASK);

		LOG.debug("NotificationReceiver called with action {0}, calendarId {1}, eventId {2}", acton, calendarId, eventId);

		EventsSource eventsSource = new EventsSource(context, calendarId);
		Event event = eventsSource.get(eventId);

		if (event == null) {
			LOG.debug("Event {0} does not exists anymore", eventId);
			return;
		}

		if (ACTION_ALARM.equals(acton)) {
			new Notifier(context).onEventAlarm(calendarId, event);
		}
	}
}
