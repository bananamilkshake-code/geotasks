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
package me.jtalk.android.geotasks.me.jtalk.android.application;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import me.jtalk.android.geotasks.application.Notifier;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;

public class NotifierTest extends AndroidTestCase {
	@MediumTest
	public void testCreateNotification() {
		Notifier notifier = new Notifier(getContext());

		TaskCoordinates currentPosition = new TaskCoordinates(23.34, 34.32);

		long calendarId = 1;

		Event event = new Event(0, "TestEvent", "Description", null, null, currentPosition, true);

		notifier.onEventIsNear(calendarId, event, currentPosition, 100);
	}
}
