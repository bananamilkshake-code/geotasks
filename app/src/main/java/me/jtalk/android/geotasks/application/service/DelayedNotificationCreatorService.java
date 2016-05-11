/*
 * Copyright (C) 2016 Liza Lukicheva
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package me.jtalk.android.geotasks.application.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.text.MessageFormat;

import me.jtalk.android.geotasks.application.receiver.NotificationReceiver;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.util.Logger;

public class DelayedNotificationCreatorService extends IntentService {

	private static final Logger LOG = new Logger(DelayedNotificationCreatorService.class);

	public DelayedNotificationCreatorService() {
		super(DelayedNotificationCreatorService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		LOG.debug("New delayed event intent received: action {0}", intent.getAction());

		intent.setComponent(null);
		intent.setExtrasClassLoader(TaskCoordinates.class.getClassLoader());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		long currentTime = intent.getLongExtra(NotificationReceiver.INTENT_EXTRA_DELAY_CURRENT_TIME, -1);
		long delayTimeInMillis = intent.getLongExtra(NotificationReceiver.INTENT_EXTRA_DELAY_TIME, -1);
		((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC, currentTime + delayTimeInMillis, pendingIntent);

		Toast.makeText(this, MessageFormat.format("Notification will be delayed for {0} minutes", delayTimeInMillis / NotificationReceiver.MILLIS_IN_MINUTE), Toast.LENGTH_SHORT).show();

		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int notificationId = intent.getIntExtra(NotificationReceiver.INTENT_EXTRA_NOTIFICATION_ID, 0);
		manager.cancel(notificationId);
	}
}