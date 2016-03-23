package me.jtalk.android.geotasks.application;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.NonNull;

import java.text.MessageFormat;

import lombok.AllArgsConstructor;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.source.Event;

@AllArgsConstructor
public class Notifier {

	private static final long[] VIBRATION_PATTERN = new long[]{1000, 0};

	private Context context;

	/**
	 * Creates and displays notification about near event.
	 *
	 * @param event
	 */
	public void onEventIsNear(Event event, double distance) {
		Notification.Builder builder = new Notification.Builder(context)
				.setContentTitle(getNotificationTitle(event))
				.setContentText(getNotificationText(distance))
				.setAutoCancel(true)
				.setVibrate(VIBRATION_PATTERN);

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
		return Icon.createWithResource(context, R.drawable.treasure_map_50);
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
