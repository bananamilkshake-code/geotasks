package me.jtalk.android.geotasks.source;

public interface EventsInserter {
	public void addEvent(String title, String description, long startTime, long endTime) throws SecurityException;
}
