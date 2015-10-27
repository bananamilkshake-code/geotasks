package me.jtalk.android.geotasks.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.util.GeoPointFormat;

public class LocationPickActivity extends Activity {
	private static final GeoPoint DEFAULT_GEOPOINT = new GeoPoint(48.8583, 2, 2944);

	public static final int INTENT_LOCATION_PICK = 0;

	public static final String INTENT_EXTRA_EDIT = "extra-is-edit";
	public static final String INTENT_EXTRA_LONGITUDE = "extra-longtitude";
	public static final String INTENT_EXTRA_LATITUDE = "extra-latitude";

	private MapView mapView;
	private IGeoPoint pickedLocation;

	private TextView textLocationName;
	private TextView textLocationCoordinates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_pick);

		initMapView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_location_pick, menu);
		return true;
	}

	// This method is called on menu_location_pick.menu_action_location_pick_save
	public void onPickClick(MenuItem item) {
		Intent result = new Intent();
		if (pickedLocation != null) {
			result.putExtra(INTENT_EXTRA_LATITUDE, pickedLocation.getLatitude());
			result.putExtra(INTENT_EXTRA_LONGITUDE, pickedLocation.getLongitude());
		}

		setResult(RESULT_OK, result);
		finish();
	}

	public void onZoomInClick(View view) {
		mapView.getController().zoomIn();
	}

	public void onZoomOutClick(View view) {
		mapView.getController().zoomOut();
	}

	private void initMapView() {
		GestureDetector gestureDetector = new GestureDetector(this, new MapGestureDetector());

		mapView = (MapView) findViewById(R.id.map);
		mapView.setTileSource(TileSourceFactory.MAPNIK);

		mapView.setMultiTouchControls(true);
		mapView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

		textLocationCoordinates = (TextView) findViewById(R.id.add_event_location_coordinates_text);
		textLocationName = (TextView) findViewById(R.id.location_name);

		Intent intent = getIntent();

		GeoPoint startPoint;
		double latitude = intent.getDoubleExtra(INTENT_EXTRA_LATITUDE, DEFAULT_GEOPOINT.getLatitude());
		double longitude = intent.getDoubleExtra(INTENT_EXTRA_LONGITUDE, DEFAULT_GEOPOINT.getLongitude());
		startPoint = new GeoPoint(latitude, longitude);

		if (intent.hasExtra(INTENT_EXTRA_EDIT)) {
			onLocationPick(startPoint);
		}

		IMapController mapController = mapView.getController();
		mapController.setZoom(9);
		mapController.setCenter(startPoint);
	}

	private void onLocationPick(IGeoPoint geoPoint) {
		pickedLocation = geoPoint;
		updateCurrentLocation(pickedLocation);
	}

	private void updateCurrentLocation(IGeoPoint pickedLocation) {
		textLocationCoordinates.setText(GeoPointFormat.format(pickedLocation));
		textLocationCoordinates.setVisibility(View.VISIBLE);

		ArrayList<OverlayItem> items = new ArrayList<>();
		items.add(new OverlayItem(null, null, pickedLocation));

		List<Overlay> overlays = mapView.getOverlays();
		overlays.clear();
		overlays.add(new ItemizedIconOverlay<>(this, items, null));

		mapView.invalidate();
	}

	private class MapGestureDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			IGeoPoint pickedPoint = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
			onLocationPick(pickedPoint);
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