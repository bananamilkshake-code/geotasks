package me.jtalk.android.geotasks.util;

import org.junit.Test;

import me.jtalk.android.geotasks.location.TaskCoordinates;

import static org.junit.Assert.assertEquals;

public class CoordinatesFormatTest {

	@Test
	public void testFormat() {
		double lat = 42.345678345345;
		double lon = 41.234;

		String result = CoordinatesFormat.formatSimple(new TaskCoordinates(lat, lon));

		assertEquals("042.345678 041.234000", result);
	}
}
