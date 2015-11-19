package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StatFs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowStatFs;

import me.jtalk.android.geotasks.BuildConfig;
import me.jtalk.android.geotasks.location.TaskCoordinates;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
		sdk = 21,
		manifest = "src/main/AndroidManifest.xml",
		shadows = {LocationPickActivityTest.ExtendedShadowStatsFs.class})
public class LocationPickActivityTest {
	/**
	 * MapView (from mapsforge library) uses StaFs to check available
	 * storage size. Robolectric does not shadows required method therefore
	 * extended shadow class must be implemented.
	 */
	@Implements(StatFs.class)
	public static class ExtendedShadowStatsFs extends ShadowStatFs {
		public ExtendedShadowStatsFs() {
		}

		public void __constructor__(String path) {
			super.__constructor__(path);
		}

		@Implementation
		public long getAvailableBytes() {
			return getAvailableBlocksLong() * getBlockSizeLong();
		}
	}

	private LocationPickActivity activity;

	@Before
	public void setUp() throws Exception {
		ExtendedShadowStatsFs.registerStats("/tmp", 10, 10, 10);

		activity = Robolectric.setupActivity(LocationPickActivity.class);
	}

	@After
	public void terminate() {
		activity.onDestroy();
	}

	@Test
	public void testActivityResultNothingWhenNoCoordinatesPicked() {
		clickSave();

		ShadowActivity shadowActivity = shadowOf(activity);

		Intent resultIntent = shadowActivity.getResultIntent();
		assertEquals(Activity.RESULT_OK, shadowActivity.getResultCode());
		assertNull(resultIntent.getExtras());
	}

	@Test
	public void testActivityResultCoordinatesWhenCoordinatesPicked() {
		AndroidGraphicFactory.createInstance(activity.getApplication());

		TaskCoordinates pickedCoordinates = new TaskCoordinates(12.23123, 32.234);
		activity.onLocationPick(pickedCoordinates);

		clickSave();

		ShadowActivity shadowActivity = shadowOf(activity);

		Intent resultIntent = shadowActivity.getResultIntent();
		Bundle resultExtras = resultIntent.getExtras();

		assertEquals(shadowActivity.getResultCode(), Activity.RESULT_OK);
		assertEquals(1, resultExtras.size());
		assertEquals(pickedCoordinates, resultExtras.get(LocationPickActivity.INTENT_EXTRA_COORDINATES));
	}

	private void clickSave() {
		activity.onPickClick(null);
	}
}
