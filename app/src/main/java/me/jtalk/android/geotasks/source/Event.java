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
package me.jtalk.android.geotasks.source;

import java.util.Calendar;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.util.CoordinatesFormat;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Event {
	@Getter
	private long id;

	@Setter
	@Getter
	private String title;

	@Setter
	@Getter
	private String description;

	@Setter
	@Getter
	private Calendar startTime;

	@Setter
	@Getter
	private Calendar endTime;

	@Setter
	@Getter
	private TaskCoordinates coordinates;

	public String getLocationText() {
		if (coordinates == null) {
			return "";
		}

		return CoordinatesFormat.format(coordinates);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof Event) {
			return this.id == ((Event) obj).getId();
		}

		return false;
	}

	static public Event copyOf(final Event event) {
		if (event == null) {
			return null;
		}

		return new Event(
				event.getId(),
				event.getTitle(),
				event.getDescription(),
				event.getStartTime(),
				event.getEndTime(),
				event.getCoordinates());
	}

	static public Event createEmpty() {
		return new Event();
	}
}
