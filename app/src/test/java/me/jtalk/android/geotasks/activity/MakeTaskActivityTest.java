package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import me.jtalk.android.geotasks.BuildConfig;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.util.CoordinatesFormat;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
		sdk = 21,
		manifest = "src/main/AndroidManifest.xml")
public class MakeTaskActivityTest {
	private MakeTaskActivity activity;

	@Before
	public void setUp() {
		activity = Robolectric.setupActivity(MakeTaskActivity.class);
	}

	@Test
	public void testStartLocationPickActivityWithoutCoordinates() {
		activity.findViewById(R.id.add_event_location_coordinates_text).performClick();

		Intent expectedIntent = new Intent(activity, LocationPickActivity.class);

		assertEquals(shadowOf(activity).getNextStartedActivity(), expectedIntent);
	}

	@Test
	public void testStartLocationPickActivityAndPassCoordinates() {
		TaskCoordinates previousCoordinates = new TaskCoordinates(12.23, 45.45);
		TextView locationCoordinatesText = (TextView) activity.findViewById(R.id.add_event_location_coordinates_text);
		locationCoordinatesText.setText(CoordinatesFormat.formatSimple(previousCoordinates));
		locationCoordinatesText.performClick();

		Intent expectedIntent = new Intent(activity, LocationPickActivity.class);
		expectedIntent.putExtra(LocationPickActivity.INTENT_EXTRA_EDIT, true);
		expectedIntent.putExtra(LocationPickActivity.INTENT_EXTRA_COORDINATES, previousCoordinates);

		assertEquals(shadowOf(activity).getNextStartedActivity(), expectedIntent);
	}

	@Test
	public void testCoordinatesPickingWithLocationPickedActivityCancelled() {
		Intent emptyIntent = new Intent();
		activity.onActivityResult(
				LocationPickActivity.INTENT_LOCATION_PICK,
				Activity.RESULT_CANCELED,
				emptyIntent);

		TextView coordinatesText = (TextView) activity.findViewById(R.id.add_event_location_coordinates_text);
		assertEquals("", coordinatesText.getText().toString());
	}

	@Test
	public void testCoordinatesPickedWithLocationPickedActivity() {
		TaskCoordinates pickedCoordinates = new TaskCoordinates(12.34, 34.23);
		String formattedCoordinates = CoordinatesFormat.formatSimple(pickedCoordinates);

		Intent intentWithCoordinates = new Intent();
		intentWithCoordinates.putExtra(LocationPickActivity.INTENT_EXTRA_COORDINATES, pickedCoordinates);

		activity.onActivityResult(
				LocationPickActivity.INTENT_LOCATION_PICK,
				Activity.RESULT_OK,
				intentWithCoordinates);

		TextView coordinatesText = (TextView) activity.findViewById(R.id.add_event_location_coordinates_text);
		assertEquals(formattedCoordinates, coordinatesText.getText().toString());
	}
}
