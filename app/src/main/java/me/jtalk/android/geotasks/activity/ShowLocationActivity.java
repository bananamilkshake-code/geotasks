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

import android.os.Bundle;
import android.view.GestureDetector;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

import java.text.MessageFormat;
import java.text.ParseException;

import butterknife.Bind;
import butterknife.ButterKnife;
import lombok.SneakyThrows;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventIntentFields;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;
import me.jtalk.android.geotasks.util.MapViewContext;

public class ShowLocationActivity extends BaseActivity implements EventIntentFields {

	private static final Logger LOG = new Logger(ShowLocationActivity.class);

	public static final int SHOW_CURRENT = 0;

	public static final String INTENT_EXTRA_CURRENT_POSITION = "current-position";

	@Bind(R.id.show_location_map)
	public MapView mapView;

	/**
	 * Shows position of event
	 */
	private Marker eventPosition;

	/**
	 * Shows current device position
	 */
	private Marker currentPosition;

	private MapViewContext mapViewContext;

	private Event event;

	@Override
	@SneakyThrows(ParseException.class)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_location);

		long eventId = getIntent().getLongExtra(INTENT_EXTRA_EVENT_ID, EventsSource.NO_TASK);
		TaskCoordinates position = getIntent().getParcelableExtra(INTENT_EXTRA_CURRENT_POSITION);

		event = retrieveEvent(eventId);
		if (event == null) {
			LOG.info("Event with id {0} does not exist", eventId);
			finish();
			return;
		}

		setTitle(MessageFormat.format(getString(R.string.title_activity_show_location), event.getTitle()));

		eventPosition = createMarker(R.drawable.ic_place_black_48dp, event.getCoordinates());
		currentPosition = createMarker(R.drawable.ic_person_pin_black_48dp, position);

		ButterKnife.bind(this);

		mapViewContext = new MapViewContext(mapView, this);

		GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener());

		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(false);
		mapView.setOnTouchListener((v, event1) -> gestureDetector.onTouchEvent(event1));
		mapView.getLayerManager().getLayers().add(mapViewContext.getTileDownloadLayer());
		mapView.getLayerManager().getLayers().add(eventPosition);
		mapView.getLayerManager().getLayers().add(currentPosition);

		setupPosition(position);
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

	private Marker createMarker(int iconId, TaskCoordinates startCoordinates) {
		Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(getDrawable(iconId));
		return new Marker(startCoordinates.toLatLong(), bitmap, 0, -bitmap.getHeight() / 2);
	}

	private Event retrieveEvent(long id) throws ParseException {
		long calendarId = getSharedPreferences().getLong(getString(R.string.pref_calendar_id), EventsSource.DEFAULT_CALENDAR);
		if (calendarId == EventsSource.DEFAULT_CALENDAR) {
			throw new IllegalStateException("No calendar is created for application");
		}

		EventsSource eventsSource = new EventsSource(this, calendarId);
		eventsSource.disable(id);

		return eventsSource.get(id);
	}

	public void setupPosition(TaskCoordinates position) {
		mapView.getModel().mapViewPosition.setZoomLevel((byte) 12); // TODO: zoom must be relative to distance between event and position
		mapView.getModel().mapViewPosition.setCenter(event.getCoordinates().toLatLong());	// TODO: setup point between event and position as center

		eventPosition.setLatLong(event.getCoordinates().toLatLong());
		currentPosition.setLatLong(position.toLatLong());
	}
}
