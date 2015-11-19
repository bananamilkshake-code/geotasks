package me.jtalk.android.geotasks.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.Model;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;

import lombok.Getter;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.listeners.MapGestureDetector;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.util.CoordinatesFormat;

public class LocationPickActivity extends Activity {
	private static final TaskCoordinates DEFAULT_COORDINATES = new TaskCoordinates(48.8583, 2.2944);

	private static final byte DEFAULT_ZOOM = 9;
	private static final byte MIN_ZOOM = 0;
	private static final byte MAX_ZOOM = 18;

	public static final int INTENT_LOCATION_PICK = 0;

	public static final String INTENT_EXTRA_EDIT = "extra-is-edit";
	public static final String INTENT_EXTRA_COORDINATES = "extra-coordinates";

	private static final String MAPSFORGE_CACHE_NAME = "mapsforge-cache";
	private static final float MAPSFORGE_SCREEN_RATIO = 1f;

	@Getter
	private MapView mapView;

	/**
	 * Coordinate that will be returned from this activity.
	 */
	@Getter
	private TaskCoordinates pickedLocation;

	/**
	 * This marker is draw at picked location.
	 */
	private Marker marker;

	private TileDownloadLayer tileDownloadLayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_pick);

		initMapView();
		initSearchEditText();
	}

	@Override
	public void onPause() {
		if (tileDownloadLayer != null) {
			tileDownloadLayer.onPause();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (tileDownloadLayer != null) {
			tileDownloadLayer.onResume();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.destroyAll();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_location_pick, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * This method is called on menu_location_pick.menu_action_location_pick_save.
	 *
	 * @param item
	 */
	public void onPickClick(MenuItem item) {
		Intent result = new Intent();
		if (pickedLocation != null) {
			result.putExtra(INTENT_EXTRA_COORDINATES, pickedLocation);
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
		mapView.getModel().mapViewPosition.zoomIn();
	}

	/**
	 * This method is called on activity_location_pick.zoom_out click.
	 *
	 * @param view
	 */
	public void onZoomOutClick(View view) {
		mapView.getModel().mapViewPosition.zoomOut();
	}

	/**
	 * Remember {@coordiates} as picked location and draw marker on map.
	 * @param coordinates
	 */
	public void onLocationPick(TaskCoordinates coordinates) {
		pickedLocation = coordinates;

		updateCurrentLocation(pickedLocation);
	}

	private void initMapView() {
		Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(getDrawable(R.drawable.ic_place_black_48dp));
		marker = new Marker(null, bitmap, 0, -bitmap.getHeight() / 2);

		GestureDetector gestureDetector = new GestureDetector(this, new MapGestureDetector(this));

		TaskCoordinates startPoint = extractStartCoordinates(getIntent());

		mapView = (MapView) findViewById(R.id.map);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(false);
		mapView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
		mapView.getModel().mapViewPosition.setZoomLevel(DEFAULT_ZOOM);
		mapView.getModel().mapViewPosition.setZoomLevelMin(MIN_ZOOM);
		mapView.getModel().mapViewPosition.setZoomLevelMax(MAX_ZOOM);
		mapView.getModel().mapViewPosition.setCenter(startPoint.toLatLong());
		mapView.getLayerManager().getLayers().add(createDownloadLayer(createTileCache(mapView.getModel())));
		mapView.getLayerManager().getLayers().add(marker);
	}

	private TileCache createTileCache(Model model) {
		return AndroidUtil.createTileCache(
				this,
				MAPSFORGE_CACHE_NAME,
				model.displayModel.getTileSize(),
				MAPSFORGE_SCREEN_RATIO,
				model.frameBufferModel.getOverdrawFactor());
	}

	private void initSearchEditText() {
		EditText searchEditText = (EditText) findViewById(R.id.location_pick_search_text_edit);
		searchEditText.setOnEditorActionListener((view, actionId, event) -> {
			if (event.getKeyCode() != KeyEvent.KEYCODE_ENTER) {
				return false;
			}

			LocationPickActivity.this.searchLocation(searchEditText.getText().toString());
			return true;
		});

		searchEditText.setOnTouchListener((view, event) -> {
			final int DRAWABLE_RIGHT = 2;

			if (event.getAction() != MotionEvent.ACTION_UP) {
				return false;
			}

			if (event.getRawX() < (searchEditText.getRight() - searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
				return false;
			}

			LocationPickActivity.this.searchLocation(searchEditText.getText().toString());
			return true;
		});
	}

	private void searchLocation(String addressText) {
		if (addressText.isEmpty()) {
			return;
		}

		TaskCoordinates searched = TaskCoordinates.search(this, addressText);
		if (searched == null) {
			EditText searchEditText = (EditText) findViewById(R.id.location_pick_search_text_edit);
			searchEditText.setError(getString(R.string.locatio_pick_incorrect_address));
		} else {
			mapView.getModel().mapViewPosition.setCenter(searched.toLatLong());
		}
	}

	/**
	 * Created layout uses data from file on sdcard
	 *
	 * @param tileCache
	 * @return layout for map view
	 */
	private Layer createRenderLayer(TileCache tileCache, String mapFilePath) {
		MapDataStore mapDataStore = new MapFile(
				new File(Environment.getExternalStorageDirectory(), mapFilePath));

		TileRendererLayer tileRenderLayer = new TileRendererLayer(
				tileCache,
				mapDataStore,
				mapView.getModel().mapViewPosition,
				false,
				true,
				AndroidGraphicFactory.INSTANCE);
		tileRenderLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

		return tileRenderLayer;
	}

	/**
	 * Created layer will load OSM data from network.
	 *
	 * @param tileCache
	 * @return layout for map view
	 */
	private Layer createDownloadLayer(TileCache tileCache) {
		tileDownloadLayer = new TileDownloadLayer(
				tileCache,
				mapView.getModel().mapViewPosition,
				OpenStreetMapMapnik.INSTANCE,
				AndroidGraphicFactory.INSTANCE);

		return tileDownloadLayer;
	}

	/**
	 * Retrieves and returns start coordinates from provided intent ({@INTENT_EXTRA_COORDINATES}.
	 * If no data was provided {@DEFAULT_COORDINATES} values will be used.
	 * If intent has {@INTENT_EXTRA_EDIT} extra retrieved coordinates will be set up
	 * as picked location.
	 *
	 * @param intent Intent, that can contain start coordinates data.
	 * @return retrieved coordinates
	 */
	private TaskCoordinates extractStartCoordinates(Intent intent) {
		TaskCoordinates startCoordinates = intent.getParcelableExtra(INTENT_EXTRA_COORDINATES);
		if (startCoordinates == null) {
			return DEFAULT_COORDINATES;
		}

		if (intent.hasExtra(INTENT_EXTRA_EDIT)) {
			onLocationPick(startCoordinates);
		}

		return startCoordinates;
	}

	/**
	 * Draws marker a {@coordinates} position.
	 *
	 * @param coordinates where marker must be drawn
	 */
	private void updateCurrentLocation(TaskCoordinates coordinates) {
		TextView textLocationCoordinates = (TextView) findViewById(R.id.add_event_location_coordinates_text);
		textLocationCoordinates.setText(CoordinatesFormat.format(coordinates));
		textLocationCoordinates.setVisibility(View.VISIBLE);

		marker.setLatLong(coordinates.toLatLong());
		marker.requestRedraw();
	}
}
