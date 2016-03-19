package me.jtalk.android.geotasks.application.listener;

import android.location.Location;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.jtalk.android.geotasks.application.Notifier;
import me.jtalk.android.geotasks.application.listeners.EventsLocationListener;
import me.jtalk.android.geotasks.location.TaskCoordinates;
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
		Location mockCurrentLocation = Mockito.mock(Location.class);

		TaskCoordinates asTaskCoordinates = new TaskCoordinates(mockCurrentLocation);

		TaskCoordinates coordinates1 = Mockito.mock(TaskCoordinates.class);
		TaskCoordinates coordinates2 = Mockito.mock(TaskCoordinates.class);
		TaskCoordinates coordinates3 = Mockito.mock(TaskCoordinates.class);

		when(coordinates1.distanceTo(asTaskCoordinates)).thenReturn(1000.0);
		when(coordinates2.distanceTo(asTaskCoordinates)).thenReturn(10.0);
		when(coordinates3.distanceTo(asTaskCoordinates)).thenReturn(10.0);

		Event event1 = new Event(1, "Event1", "Desc1", Calendar.getInstance(), coordinates1);
		Event event2 = new Event(2, "Event2", "Desc2", Calendar.getInstance(), coordinates2);
		Event event3 = new Event(3, "Event3", "Desc3", Calendar.getInstance(), coordinates3);

		List<Event> activeEvents = new ArrayList<>();
		activeEvents.add(event1);
		activeEvents.add(event2);
		activeEvents.add(event3);

		EventsSource mockEventsSource = Mockito.mock(EventsSource.class);
		when(mockEventsSource.getActive(any(Calendar.class))).thenReturn(activeEvents);

		Notifier mockNotifier = Mockito.mock(Notifier.class);
		doNothing().when(mockNotifier).onEventIsNear(any());

		EventsLocationListener eventLocationListener = new EventsLocationListener(mockEventsSource, mockNotifier);
		eventLocationListener.setDistanceToAlarm(100);

		eventLocationListener.onLocationChanged(mockCurrentLocation);

		verify(mockEventsSource).getActive(any(Calendar.class));
		verifyNoMoreInteractions(mockEventsSource);

		verify(mockNotifier).onEventIsNear(event2);
		verify(mockNotifier).onEventIsNear(event3);
		verifyNoMoreInteractions(mockNotifier);
	}
}
