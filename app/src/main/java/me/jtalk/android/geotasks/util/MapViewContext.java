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
package me.jtalk.android.geotasks.util;

import android.content.Context;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.model.Model;

import lombok.Getter;

public class MapViewContext {

	private static final String MAPSFORGE_CACHE_NAME = "mapsforge-cache";
	private static final float MAPSFORGE_SCREEN_RATIO = 1f;

	@Getter
	private TileDownloadLayer tileDownloadLayer;

	/**
	 * Created layer will load OSM data from network.
	 *
	 * @return layout for map view
	 */
	public MapViewContext(MapView mapView, Context context) {
		TileCache tileCache = createTileCache(context, mapView.getModel());
		tileDownloadLayer = new TileDownloadLayer(
				tileCache,
				mapView.getModel().mapViewPosition,
				OpenStreetMapMapnik.INSTANCE,
				AndroidGraphicFactory.INSTANCE);
	}

	public void pause() {
		if (tileDownloadLayer != null) {
			tileDownloadLayer.onPause();
		}
	}

	public void resume() {
		if (tileDownloadLayer != null) {
			tileDownloadLayer.onResume();
		}
	}

	private TileCache createTileCache(Context context, Model model) {
		return AndroidUtil.createTileCache(
				context,
				MAPSFORGE_CACHE_NAME,
				model.displayModel.getTileSize(),
				MAPSFORGE_SCREEN_RATIO,
				model.frameBufferModel.getOverdrawFactor());
	}
}
