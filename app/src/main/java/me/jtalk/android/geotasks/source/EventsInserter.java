package me.jtalk.android.geotasks.source;

public interface EventsInserter {
	public void addEvent(String title, String description, String location, long startTime, long endTime) throws SecurityException;
}
