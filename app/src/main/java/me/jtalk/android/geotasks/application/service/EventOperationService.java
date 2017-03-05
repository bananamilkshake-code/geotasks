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
package me.jtalk.android.geotasks.application.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import me.jtalk.android.geotasks.source.EventIntentFields;
import me.jtalk.android.geotasks.source.EventsSource;

import static me.jtalk.android.geotasks.source.EventIntentFields.INTENT_EXTRA_CALENDAR_ID;
import static me.jtalk.android.geotasks.source.EventIntentFields.INTENT_EXTRA_EVENT_ID;

public class EventOperationService extends IntentService {

	public static final int INTENT_DISABLE_EVENT = 0;

	public static final String INTENT_EXTRA_NOTIFICATION_ID = "notification-id";

	public EventOperationService() {
		super(EventOperationService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		long calendarId = intent.getLongExtra(INTENT_EXTRA_CALENDAR_ID, EventsSource.DEFAULT_CALENDAR);
		long eventId = intent.getLongExtra(INTENT_EXTRA_EVENT_ID, EventsSource.NO_TASK);
		int notificationId = intent.getIntExtra(INTENT_EXTRA_NOTIFICATION_ID, 0);

		if (calendarId == EventsSource.DEFAULT_CALENDAR) {
			throw new IllegalArgumentException("Calendar id for service is incorrect or not passed");
		}

		if (eventId == EventsSource.NO_TASK) {
			throw new IllegalArgumentException("Event id for service is incorrect or not passed");
		}

		EventsSource eventsSource = new EventsSource(this, calendarId);
		eventsSource.disable(eventId);

		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(notificationId);
	}
}