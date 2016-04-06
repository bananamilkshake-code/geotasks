package me.jtalk.android.geotasks.util;

import org.junit.Test;

import me.jtalk.android.geotasks.location.TaskCoordinates;

import static org.junit.Assert.assertEquals;

public class CoordinatesFormatTest {

	@Test
	public void testFormat() {
		double lat = 42.345678345345;
		double lon = 41.234;

		String result = CoordinatesFormat.formatForDatabase(new TaskCoordinates(lat, lon));

		assertEquals("042.3456783453450000 041.2340000000000000", result);
	}
}
