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

import me.jtalk.android.geotasks.application.service.Permission;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Consumer;

public abstract class BaseCalendarActivity extends BaseActivity {

    private EventsSource eventsSource;

    protected void withEventSource(Consumer<EventsSource> f) {
         if (eventsSource == null) {
             withPermissionsAsync(Permission.MANAGE_CALENDAR, () -> {
                 if (eventsSource == null) {
                     eventsSource = initEventsSource();
                 }
                 f.consume(eventsSource);
             });
         } else {
             f.consume(eventsSource);
         }
    }

    protected abstract EventsSource initEventsSource();
}
