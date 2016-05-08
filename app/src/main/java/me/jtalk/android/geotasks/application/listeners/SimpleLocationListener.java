/*
 * Copyright (C) 2016 Liza Lukicheva
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package me.jtalk.android.geotasks.application.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.location.LocationProvider;

public abstract class SimpleLocationListener implements LocationListener {

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {
		String status = null;
		switch (i) {
			case LocationProvider.AVAILABLE:
				status = "AVAILABLE";
				break;
			case LocationProvider.OUT_OF_SERVICE:
				status = "OUT_OF_SERVICE";
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				status = "TEMPORARILY_UNAVAILABLE";
				break;
		}

		Log.d(SimpleLocationListener.class.getName(), "Provider " + s + " status changed: new " + status);
	}

	@Override
	public void onProviderEnabled(String s) {
		Log.d(SimpleLocationListener.class.getName(), "Provider " + s + " enabled");

	}

	@Override
	public void onProviderDisabled(String s) {
		Log.d(SimpleLocationListener.class.getName(), "Provider " + s + " disabled");
	}
}
