package me.jtalk.android.geotasks.application.listener;

import android.location.Location;

import org.junit.Test;
import org.mockito.Mockito;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.jtalk.android.geotasks.application.Notifier;
import me.jtalk.android.geotasks.application.listeners.EventsLocationListener;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class EventsLocationListenerTest {

	@Test
	public void testNearLocationChoose() {
		Event event1 = new Event(1, "Event1", "Time1", new GeoPoint(0.0, 0.0));
		Event event2 = new Event(2, "Event2", "Time2", new GeoPoint(55.63947, 27.03267));
		Event event3 = new Event(3, "Event3", "Time3", new GeoPoint(55.63847, 27.03257));

		List<Event> activeEvents = new ArrayList<>();
		activeEvents.add(event1);
		activeEvents.add(event2);
		activeEvents.add(event3);

		EventsSource mockEventsSource = Mockito.mock(EventsSource.class);
		when(mockEventsSource.getActive(any(Calendar.class))).thenReturn(activeEvents);

		Notifier mockNotifier = Mockito.mock(Notifier.class);
		doNothing().when(mockNotifier).onEventIsNear(any());

		EventsLocationListener eventLocationListener = new EventsLocationListener();
		eventLocationListener.setDistanceToAlarm(100);
		eventLocationListener.setEventsSource(mockEventsSource);
		eventLocationListener.setNotifier(mockNotifier);

		Location mockCurrentLocation = Mockito.mock(Location.class);
		when(mockCurrentLocation.getLatitude()).thenReturn(55.63914);
		when(mockCurrentLocation.getLongitude()).thenReturn(27.03195);

		eventLocationListener.onLocationChanged(mockCurrentLocation);

		verify(mockEventsSource).getActive(any(Calendar.class));
		verifyNoMoreInteractions(mockEventsSource);

		verify(mockNotifier).onEventIsNear(event2);
		verify(mockNotifier).onEventIsNear(event3);
		verifyNoMoreInteractions(mockNotifier);
	}
}
