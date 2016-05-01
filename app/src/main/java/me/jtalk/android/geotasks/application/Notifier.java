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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import java.text.MessageFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.activity.ShowLocationActivity;
import me.jtalk.android.geotasks.application.service.EventOperationService;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.util.Logger;

//TODO: move this class logic to Notifier service

@AllArgsConstructor
public class Notifier {

	private static final Logger LOG = new Logger(Notifier.class);

	private static final long[] VIBRATION_PATTERN = new long[]{1000, 0};

	@Getter
	private Context context;

	/**
	 * Creates and displays notification about near event.
	 * @param calendarId calendar event witch event belongs to
	 * @param event event to notify about
	 * @param currentPosition location where notification is fired
	 * @param distance calculated distance to event
	 */
	public void onEventIsNear(long calendarId, Event event, TaskCoordinates currentPosition, double distance) {
		final int notificationId = getNotificationId(event);

		Intent intent = new Intent(context, ShowLocationActivity.class);
		intent.putExtra(ShowLocationActivity.INTENT_EXTRA_EVENT_ID, event.getId());
		intent.putExtra(ShowLocationActivity.INTENT_EXTRA_CURRENT_POSITION, currentPosition);
		PendingIntent openLocationIntent = PendingIntent.getActivity(context, ShowLocationActivity.SHOW_CURRENT, intent, 0);

		final String title = MessageFormat.format(context.getString(R.string.notification_event_is_near_title_arg_1), event.getTitle());
		final String contentText = MessageFormat.format(context.getString(R.string.notification_event_is_near_text_arg_1), distance);

		Notification.Builder builder =
				createNotificationBuilder(calendarId, event.getId(), notificationId, title, contentText)
				.setContentIntent(openLocationIntent);

		getNotificationManager().notify(notificationId, builder.build());
	}

	/**
	 * Creates and displays notification about timing event.
	 * @param calendarId calendar event witch event belongs to
	 * @param event event to notify about
	 */
	public void onEventAlarm(long calendarId, Event event) {
		LOG.debug("Notify about timing event {0} from calendar {1}", event.getId(), calendarId);

		final int notificationId = getNotificationId(event);

		final String title = MessageFormat.format(context.getString(R.string.notification_event_reminder_title), event.getTitle());
		final String contentText = event.getDescription() != null ? MessageFormat.format(context.getString(R.string.notification_event_reminder_text), event.getDescription()) : null;

		Notification.Builder builder = createNotificationBuilder(calendarId, event.getId(), notificationId, title, contentText);

		getNotificationManager().notify(notificationId, builder.build());
	}

	private Notification.Builder createNotificationBuilder(long calendarId, long eventId, int notificationId, String title, String contentText) {
		Notification.Builder builder = new Notification.Builder(context)
				.setContentTitle(title)
				.setAutoCancel(true)
				.setVibrate(VIBRATION_PATTERN)
				.setSound(getSound())
				.addAction(createDisableAction(calendarId, eventId, notificationId));

		if (contentText != null) {
			builder.setContentText(contentText);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			builder.setSmallIcon(getSmallIcon());
			builder.setLargeIcon(getLargeIcon());
			builder.setCategory(Notification.CATEGORY_REMINDER);
		}

		return builder;
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Notification.Action createDisableAction(long calendarId, long eventId, int notificationId) {
		final Icon icon = getDisableActionIcon();
		final String title = context.getString(R.string.notification_action_disable);
		final Intent intent = new Intent(context, EventOperationService.class);
		intent.putExtra(EventOperationService.INTENT_EXTRA_CALENDAR_ID, calendarId);
		intent.putExtra(EventOperationService.INTENT_EXTRA_EVENT_ID, eventId);
		intent.putExtra(EventOperationService.INTENT_EXTRA_NOTIFICATION_ID, notificationId);
		PendingIntent pendingIntent = PendingIntent.getService(context, EventOperationService.INTENT_DISABLE_EVENT, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return new Notification.Action.Builder(icon, title, pendingIntent).build();
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Icon getSmallIcon() {
		return Icon.createWithResource(context, R.drawable.ic_beenhere_black_18dp);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Icon getLargeIcon() {
		return Icon.createWithResource(context, R.drawable.treasure_map_colour_50);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Icon getDisableActionIcon() {
		return Icon.createWithResource(context, R.drawable.ic_alarm_off_black_18dp);
	}

	private Uri getSound() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String alarm = settings.getString(context.getString(R.string.pref_alarm_sound), RingtoneManager.getDefaultUri(Notification.DEFAULT_SOUND).toString());
		return Uri.parse(alarm);
	}

	private int getNotificationId(Event event) {
		return (int) event.getId();
	}

	private NotificationManager getNotificationManager() {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
}
