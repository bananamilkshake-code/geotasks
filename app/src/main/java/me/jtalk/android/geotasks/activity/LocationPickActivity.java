package me.jtalk.android.geotasks.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.util.CoordinatesFormat;

public class LocationPickActivity extends Activity {
	private static final TaskCoordinates DEFAULT_COORDINATES = new TaskCoordinates(48.8583, 2.2944);
	private static final int DEFAULT_ZOOM = 9;

	public static final int INTENT_LOCATION_PICK = 0;

	public static final String INTENT_EXTRA_EDIT = "extra-is-edit";
	public static final String INTENT_EXTRA_LONGITUDE = "extra-longtitude";
	public static final String INTENT_EXTRA_LATITUDE = "extra-latitude";

	private MapView mapView;
	private TaskCoordinates pickedLocation;

	private TextView textLocationCoordinates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_pick);

		textLocationCoordinates = (TextView) findViewById(R.id.add_event_location_coordinates_text);

		initMapView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_location_pick, menu);
		return true;
	}

	/**
	 * This method is called on menu_location_pick.menu_action_location_pick_save.
	 *
	 * @param item
	 */
	public void onPickClick(MenuItem item) {
		Intent result = new Intent();
		if (pickedLocation != null) {
			result.putExtra(INTENT_EXTRA_LATITUDE, pickedLocation.getLatitude());
			result.putExtra(INTENT_EXTRA_LONGITUDE, pickedLocation.getLongitude());
		}

		setResult(RESULT_OK, result);
		finish();
	}

	/**
	 * This method is called on activity_location_pick.zoom_in click.
	 *
	 * @param view
	 */
	public void onZoomInClick(View view) {
		mapView.getController().zoomIn();
	}

	/**
	 * This method is called on activity_location_pick.zoom_out click.
	 *
	 * @param view
	 */
	public void onZoomOutClick(View view) {
		mapView.getController().zoomOut();
	}

	private void initMapView() {
		GestureDetector gestureDetector = new GestureDetector(this, new MapGestureDetector());

		mapView = (MapView) findViewById(R.id.map);
		mapView.setTileSource(TileSourceFactory.MAPNIK);
		mapView.setMultiTouchControls(true);
		mapView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

		TaskCoordinates startPoint = extractStartCoordinates(getIntent());
		IMapController mapController = mapView.getController();
		mapController.setZoom(DEFAULT_ZOOM);
		mapController.setCenter(startPoint.toGeoPoint());
	}

	/**
	 * Retrieves and returns start coordinates from provided intent ({@INTENT_EXTRA_LATITUDE}
	 * and {@INTENT_EXTRA_LONGITUDE} will be checked). If no data was provided
	 * {@DEFAULT_COORDINATES} values will be used.
	 * If intent has {@INTENT_EXTRA_EDIT} extra retrieved geopoint will be set up
	 * as picked location.
	 *
	 * @param intent Intent, that can contain start geopoint data.
	 * @return retrieved geopoint
	 */
	private TaskCoordinates extractStartCoordinates(Intent intent) {
		double latitude = intent.getDoubleExtra(INTENT_EXTRA_LATITUDE, DEFAULT_COORDINATES.getLatitude());
		double longitude = intent.getDoubleExtra(INTENT_EXTRA_LONGITUDE, DEFAULT_COORDINATES.getLongitude());
		TaskCoordinates startPoint = new TaskCoordinates(latitude, longitude);

		if (intent.hasExtra(INTENT_EXTRA_EDIT)) {
			onLocationPick(startPoint);
		}

		return startPoint;
	}

	private void onLocationPick(TaskCoordinates coordinates) {
		pickedLocation = coordinates;

		updateCurrentLocation(pickedLocation);
	}

	private void updateCurrentLocation(TaskCoordinates coordinates) {
		textLocationCoordinates.setText(CoordinatesFormat.format(coordinates));
		textLocationCoordinates.setVisibility(View.VISIBLE);

		ArrayList<OverlayItem> items = new ArrayList<>();
		items.add(new OverlayItem(null, null, coordinates.toGeoPoint()));

		List<Overlay> overlays = mapView.getOverlays();
		overlays.clear();
		overlays.add(new ItemizedIconOverlay<>(this, items, null));

		mapView.invalidate();
	}

	/**
	 * Mark on the map must be placed if only single tap has occurred. OnTouchEvent doesn't provide
	 * convenient way to detect if touch was single, that's why implementation of GestureListener is
	 * needed: GestureDetector can identify click type more precisely and single tap can be catched
	 * easily.
	 */
	private class MapGestureDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			IGeoPoint pickedGeoPoint = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
			onLocationPick(new TaskCoordinates(pickedGeoPoint));
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent event1, MotionEvent event2, final float distanceX, float distanceY) {
			return super.onScroll(event1, event2, distanceX, distanceY);
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
			return super.onFling(event1, event2, velocityX, velocityY);
		}
	}
}
