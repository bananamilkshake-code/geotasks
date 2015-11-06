package me.jtalk.android.geotasks.me.jtalk.android.application;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import me.jtalk.android.geotasks.application.Notifier;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;

public class NotifierTest extends AndroidTestCase {
	@MediumTest
	public void testCreateNotification() {
		Notifier notifier = new Notifier(getContext());

		Event event = new Event(0, "TestEvent", "2015/11/03 10:57AM", new TaskCoordinates(23.34, 34.32));

		notifier.onEventIsNear(event);
	}
}
