package me.jtalk.android.geotasks.util;

import android.test.suitebuilder.annotation.SmallTest;

import org.osmdroid.util.GeoPoint;

import static junit.framework.Assert.assertEquals;

public class GeoPointFormatTest {
	@SmallTest
	public void testFormat() {
		double lat = 2.345678;
		double lon = 1.234567;

		String result = GeoPointFormat.formatSimple(new GeoPoint(lat,lon));

		assertEquals("2.345678 1.234567", result);
	}
}
