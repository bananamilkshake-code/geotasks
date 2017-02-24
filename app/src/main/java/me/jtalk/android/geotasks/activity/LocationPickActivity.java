/*
 * Copyright (C) 2016 Liza Lukicheva
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package me.jtalk.android.geotasks.activity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.listeners.MapGestureDetector;
import me.jtalk.android.geotasks.application.listeners.SimpleLocationListener;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.util.CoordinatesFormat;
import me.jtalk.android.geotasks.util.Logger;
import me.jtalk.android.geotasks.util.MapViewContext;

public class LocationPickActivity extends Activity {
	private static final Logger LOG = new Logger(LocationPickActivity.class);

	private static final TaskCoordinates DEFAULT_COORDINATES = new TaskCoordinates(48.8583, 2.2944);

	private static final byte DEFAULT_ZOOM = 9;
	private static final byte MIN_ZOOM = 0;
	private static final byte MAX_ZOOM = 18;

	public static final int INTENT_LOCATION_PICK = 0;

	public static final String INTENT_EXTRA_EDIT = "extra-is-edit";
	public static final String INTENT_EXTRA_COORDINATES = "extra-coordinates";

	@Getter
	@Bind(R.id.location_pick_map)
	MapView mapView;

	@Bind(R.id.location_pick_search)
	EditText searchEditText;

	@Bind(R.id.location_pick_coordinates)
	TextView textLocationCoordinates;

	/**
	 * Coordinate that will be returned from this activity.
	 */
	@Getter
	private TaskCoordinates pickedLocation;

	/**
	 * This marker is draw at picked location.
	 */
	private Marker marker;

	private MapViewContext mapViewContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_pick);

		initMapView();
		initSearchEditText();
		initLocationCoordinatesText();
	}

	@Override
	public void onPause() {
		mapViewContext.pause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mapViewContext.resume();
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
	 */
	public void onZoomInClick(View view) {
		mapView.getModel().mapViewPosition.zoomIn();
	}

	/**
	 * This method is called on activity_location_pick.zoom_out click.
	 */
	public void onZoomOutClick(View view) {
		mapView.getModel().mapViewPosition.zoomOut();
	}

	/**
	 * Remember {@coordiates} as picked location and draw marker on map.
	 *
	 * @param coordinates
	 */
	public void onLocationPick(TaskCoordinates coordinates) {
		pickedLocation = coordinates;

		updateCurrentLocation(pickedLocation);
	}

	private void initMapView() {
		Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(getDrawable(R.drawable.ic_place_black_48dp));
		marker = new Marker(null, bitmap, 0, -bitmap.getHeight() / 2);

		ButterKnife.bind(this);

		GestureDetector gestureDetector = new GestureDetector(this, new MapGestureDetector(this));

		mapViewContext = new MapViewContext(mapView, this);

		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(false);
		mapView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
		mapView.getModel().mapViewPosition.setZoomLevel(DEFAULT_ZOOM);
		mapView.getModel().mapViewPosition.setZoomLevelMin(MIN_ZOOM);
		mapView.getModel().mapViewPosition.setZoomLevelMax(MAX_ZOOM);
		mapView.getLayerManager().getLayers().add(mapViewContext.getTileDownloadLayer());
		mapView.getLayerManager().getLayers().add(marker);

		updateCenter(getIntent());
	}

	private void initSearchEditText() {
		searchEditText.setOnEditorActionListener((view, actionId, event) -> {
			if (event == null) { // Event triggered by ENTER key
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

	private void initLocationCoordinatesText() {
		textLocationCoordinates.setOnClickListener(new LocationDialogViewOnClickListener());
	}

	@AllArgsConstructor
	private class NumericValueFilter implements InputFilter {
		private double min;
		private double max;

		private NumberFormat format;

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			String newValue = dest.subSequence(0, dstart).toString() + source.subSequence(start, end) + dest.subSequence(dend, dest.length()).toString();
			if (newValue.length() == 0) {
				return null;
			}

			try {
				Double newNumericValue = format.parse(newValue).doubleValue();
				if (min <= newNumericValue && newNumericValue <= max) {
					return null;
				}
			} catch (ParseException exception) {
				LOG.warn(exception, "String {0} cannot be parsed to float in NumericValueFilter", newValue);
			}

			return dest.subSequence(dstart, dend);
		}
	}

	public class LocationDialogViewOnClickListener implements View.OnClickListener, Validator.ValidationListener {
		@NotEmpty
		@Bind(R.id.dialog_location_latitude)
		TextView latitudeText;

		@NotEmpty
		@Bind(R.id.dialog_location_longitude)
		TextView longitudeText;

		private Validator validator;

		private AlertDialog dialog;

		private NumberFormat format = CoordinatesFormat.getFormatForCoordinate(LocationPickActivity.this);

		@Override
		public void onClick(View view) {
			LayoutInflater inflater = LocationPickActivity.this.getLayoutInflater();
			View dialogView = inflater.inflate(R.layout.dialog_location, null);

			ButterKnife.bind(this, dialogView);

			validator = new Validator(this);
			validator.setValidationListener(this);

			latitudeText.setFilters(new InputFilter[]{new NumericValueFilter(-90.0, 90.0, format)});
			longitudeText.setFilters(new InputFilter[]{new NumericValueFilter(-180.0, 180.0, format)});

			if (pickedLocation != null) {
				latitudeText.setText(format.format(pickedLocation.getLatitude()));
				longitudeText.setText(format.format(pickedLocation.getLongitude()));
			}

			AlertDialog.Builder builder =
					new AlertDialog.Builder(LocationPickActivity.this)
							.setView(dialogView)
							.setNeutralButton(R.string.location_pick_dialog_location_setup, (d, w) -> validator.validate())
							.setNegativeButton(R.string.location_pick_dialog_location_cancel, null);
			dialog = builder.create();
			dialog.show();
		}

		@Override
		public void onValidationSucceeded() {
			Float latitude = Float.parseFloat(latitudeText.getText().toString());
			Float longitude = Float.parseFloat(longitudeText.getText().toString());

			TaskCoordinates coordinates = new TaskCoordinates(latitude, longitude);
			LocationPickActivity.this.onLocationPick(coordinates);
			LocationPickActivity.this.setupCenter(coordinates);

			dialog.dismiss();
		}

		@Override
		public void onValidationFailed(List<ValidationError> errors) {
			for (ValidationError error : errors) {
				View errorView = error.getView();
				if (errorView == latitudeText || errorView == longitudeText) {
					((TextView) errorView).setError(getString(R.string.location_pick_dialog_location_error));
				}
			}
		}
	}

	private void searchLocation(String addressText) {
		if (addressText.isEmpty()) {
			return;
		}

		TaskCoordinates searched = TaskCoordinates.search(this, addressText);
		if (searched == null) {
			searchEditText.setError(getString(R.string.location_pick_incorrect_address));
		} else {
			setupCenter(searched);
		}
	}

	/**
	 * Retrieves and setups as map center start coordinates from provided intent ({@INTENT_EXTRA_COORDINATES}.
	 * If no data was provided {@DEFAULT_COORDINATES} values will be used.
	 * If intent has {@INTENT_EXTRA_EDIT} extra  and coordinates were passed retrieved coordinates will be set up
	 * as picked location.
	 *
	 * @param intent Intent, that can contain start coordinates data.
	 */
	private void updateCenter(Intent intent) {
		TaskCoordinates coordinates = intent.getParcelableExtra(INTENT_EXTRA_COORDINATES);
		if (coordinates == null) {
			getCurrentCoordinates();
			return;
		}

		if (intent.hasExtra(INTENT_EXTRA_EDIT)) {
			setupCenter(coordinates);
			onLocationPick(coordinates);
		}
	}

	private static final String SINGLE_LOCATION_UPDATE_ACTION = "me.jtalk.geotasks.SINGLE_LOCATION_UPDATE";

	/**
	 * Requests current location from LocationManager and updates
	 * map center with it (when location is received).
	 */
	private void getCurrentCoordinates() {
		BroadcastReceiver singleUpdateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LOG.debug("Received new request");
				Location location = (Location)intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
				TaskCoordinates coordinates = new TaskCoordinates(location);
				setupCenter(coordinates);
				context.unregisterReceiver(this);
			}
		};

		registerReceiver(singleUpdateReceiver, new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION));

		Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);
		PendingIntent singleUpdatePendingIntent = PendingIntent.getBroadcast(this, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_LOW);

		String locationProvider = locationManager.getBestProvider(criteria, true);

		try {
			Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
			LOG.debug("Last known location is {0}. Provider is {1}", lastKnownLocation, locationProvider);
			LOG.debug("Trying to obtain current position");
			locationManager.requestSingleUpdate(criteria, singleUpdatePendingIntent);
			locationManager.requestSingleUpdate(criteria,
					new SimpleLocationListener() {
						@Override
						public void onLocationChanged(Location location) {
							setupCenter(new TaskCoordinates(location));
						}
					},
					null);
		} catch (SecurityException exception) {
			LOG.error("No permission to get current location");
			setupCenter(DEFAULT_COORDINATES);
		}
	}

	private void setupCenter(TaskCoordinates coordinates) {
		mapView.getModel().mapViewPosition.setCenter(coordinates.toLatLong());
	}

	/**
	 * Draws marker a {@coordinates} position.
	 *
	 * @param coordinates where marker must be drawn
	 */
	private void updateCurrentLocation(TaskCoordinates coordinates) {
		textLocationCoordinates.setText(CoordinatesFormat.prettyFormat(coordinates));

		marker.setLatLong(coordinates.toLatLong());
		marker.requestRedraw();
	}
}
