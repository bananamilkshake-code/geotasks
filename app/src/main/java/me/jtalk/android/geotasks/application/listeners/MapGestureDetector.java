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
package me.jtalk.android.geotasks.application.listeners;

import android.view.GestureDetector;
import android.view.MotionEvent;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.util.MapViewProjection;

import lombok.AllArgsConstructor;
import me.jtalk.android.geotasks.activity.LocationPickActivity;
import me.jtalk.android.geotasks.location.TaskCoordinates;

/**
 * Mark on the map must be placed if only single tap has occurred. OnTouchEvent doesn't provide
 * convenient way to detect if touch was single, that's why implementation of GestureListener is
 * needed: GestureDetector can identify click type more precisely and single tap can be caught
 * easily.
 */
@AllArgsConstructor
public class MapGestureDetector extends GestureDetector.SimpleOnGestureListener {
	private LocationPickActivity locationPickActivity;

	@Override
	public boolean onSingleTapConfirmed(MotionEvent event) {
		LatLong pickedLatLong = new MapViewProjection(locationPickActivity.getMapView()).fromPixels(event.getX(), event.getY());
		locationPickActivity.onLocationPick(new TaskCoordinates(pickedLatLong));
		return true;
	}
}