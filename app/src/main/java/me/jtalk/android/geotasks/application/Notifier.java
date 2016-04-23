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
import android.support.annotation.NonNull;

import java.text.MessageFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.activity.ShowLocationActivity;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;

@AllArgsConstructor
public class Notifier {

	private static final long[] VIBRATION_PATTERN = new long[]{1000, 0};

	@Getter
	private Context context;

	/**
	 * Creates and displays notification about near event.
	 *
	 * @param event
	 */
	public void onEventIsNear(Event event, TaskCoordinates currentPosition, double distance) {
		Intent intent = new Intent(context, ShowLocationActivity.class);
		intent.putExtra(ShowLocationActivity.INTENT_EXTRA_EVENT_ID, event.getId());
		intent.putExtra(ShowLocationActivity.INTENT_EXTRA_CURRENT_POSITION, currentPosition);
		PendingIntent openLocationIntent = PendingIntent.getActivity(context, ShowLocationActivity.SHOW_CURRENT, intent, 0);

		Notification.Builder builder = new Notification.Builder(context)
				.setContentTitle(getNotificationTitle(event))
				.setContentText(getNotificationText(distance))
				.setAutoCancel(true)
				.setVibrate(VIBRATION_PATTERN)
				.setSound(getSound())
				.setContentIntent(openLocationIntent);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			builder.setSmallIcon(getSmallIcon());
			builder.setLargeIcon(getLargeIcon());
		}

		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(getNotificationId(event), builder.build());
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Icon getSmallIcon() {
		return Icon.createWithResource(context, R.drawable.ic_beenhere_black_18dp);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Icon getLargeIcon() {
		return Icon.createWithResource(context, R.drawable.treasure_map_colour_50);
	}

	private Uri getSound() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String alarm = settings.getString(context.getString(R.string.pref_alarm_sound), RingtoneManager.getDefaultUri(Notification.DEFAULT_SOUND).toString());
		return Uri.parse(alarm);
	}

	private int getNotificationId(Event event) {
		return (int) event.getId();
	}

	/**
	 * Creates formatted title for event notification.
	 *
	 * @param event
	 * @return title string
	 */
	private String getNotificationTitle(@NonNull Event event) {
		return MessageFormat.format(context.getString(R.string.notification_event_is_near_title_arg_1), event.getTitle());
	}

	/**
	 * Creates formatted text for event notification.
	 *
	 * @param distance in meters to event
	 * @return text string
	 */
	private String getNotificationText(double distance) {
		return MessageFormat.format(context.getString(R.string.notification_event_is_near_text_arg_1), distance);
	}
}
