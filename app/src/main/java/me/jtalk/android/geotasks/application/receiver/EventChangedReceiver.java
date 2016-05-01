package me.jtalk.android.geotasks.application.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import lombok.NoArgsConstructor;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;

@NoArgsConstructor
public class EventChangedReceiver extends BroadcastReceiver {

	private static final Logger LOG = new Logger(EventChangedReceiver.class);

	@Override
	public void onReceive(Context context, Intent intent) {
		int action = intent.getIntExtra(EventsSource.INTENT_EXTRA_ACTION, EventsSource.ACTION_NONE);
		long calendarId = intent.getLongExtra(EventsSource.INTENT_EXTRA_CALENDAR_ID, EventsSource.DEFAULT_CALENDAR);
		long eventId = intent.getLongExtra(EventsSource.INTENT_EXTRA_EVENT_ID, EventsSource.NO_TASK);

		LOG.debug("EventChangedReceiver received new Intent: action {0}, calendar id {1}, eventId {2}", action, calendarId, eventId);

		switch (action) {
			case EventsSource.ACTION_ADD:
				setupAlarm(context, calendarId, eventId);
				break;
			case EventsSource.ACTION_EDIT:
				cancelAlarm(context, calendarId, eventId);
				setupAlarm(context, calendarId, eventId);
				break;
			case EventsSource.ACTION_REMOVED:
				cancelAlarm(context, calendarId, eventId);
				break;
		}
	}

	public void setupAlarm(Context context, long calendarId, long eventId) {
		EventsSource eventsSource = new EventsSource(context, calendarId);
		Event event = eventsSource.get(eventId);
		if (event.isHasAlarms()) {
			LOG.debug("Creating alarm for event {0} in calendar {1}", eventId, calendarId);
			PendingIntent intent = createEventIntent(context, calendarId, eventId);
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC, event.getStartTime().getTimeInMillis(), intent);
		}
	}

	public void cancelAlarm(Context context, long calendarId, long eventId) {
		LOG.debug("Disabling alarm for event {0} in calendar {1}", eventId, calendarId);
		PendingIntent intent = createEventIntent(context, calendarId, eventId);
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(intent);
	}

	private PendingIntent createEventIntent(Context context, long calendarId, long eventId) {
		Intent intent = new Intent(NotificationReceiver.ACTION_ALARM);
		intent.putExtra(NotificationReceiver.INTENT_EXTRA_CALENDAR_ID, calendarId);
		intent.putExtra(NotificationReceiver.INTENT_EXTRA_EVENT_ID, eventId);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}
}