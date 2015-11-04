package me.jtalk.android.geotasks.util;

import junit.framework.TestCase;

import org.junit.Test;
import org.osmdroid.util.GeoPoint;

public class GeoPointFormatTest extends TestCase {
	@Test
	public void testFormat() {
		double lat = 42.345678345345;
		double lon = 41.234;

		String result = GeoPointFormat.formatSimple(new GeoPoint(lat, lon));

		assertEquals("042.345678 041.234000", result);
	}
}
