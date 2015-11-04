package me.jtalk.android.geotasks.util;

import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import static org.junit.Assert.assertEquals;

public class GeoPointFormatTest {

	@Test
	public void testFormat() {
		double lat = 42.345678345345;
		double lon = 41.234;

		String result = GeoPointFormat.formatSimple(new GeoPoint(lat, lon));

		assertEquals("042.345678 041.234000", result);
	}
}
