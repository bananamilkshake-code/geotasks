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
package me.jtalk.android.geotasks.activity.item;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.activity.MakeTaskActivity;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.CursorHelper;
import me.jtalk.android.geotasks.util.StringValueExtractor;

import static java.text.MessageFormat.format;
import static me.jtalk.android.geotasks.util.CoordinatesFormat.prettyFormat;
import static me.jtalk.android.geotasks.util.TimeFormat.formatDateTime;

public class EventElementAdapter extends CursorTreeAdapter {
	private final int ACTIVE_EVENT_PRIMARY_COLOR;
	private final int ACTIVE_EVENT_COLOUR;

	private final int INACTIVE_EVENT_COLOUR;

	public EventElementAdapter(Context context) {
		super(null, context, true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			INACTIVE_EVENT_COLOUR = context.getResources().getColor(R.color.inactive_event, context.getTheme());

			ACTIVE_EVENT_COLOUR = context.getResources().getColor(R.color.secondary_text, context.getTheme());
			ACTIVE_EVENT_PRIMARY_COLOR = context.getResources().getColor(R.color.primary_text, context.getTheme());
		} else {
			INACTIVE_EVENT_COLOUR = context.getResources().getColor(R.color.inactive_event);

			ACTIVE_EVENT_COLOUR = context.getResources().getColor(R.color.secondary_text);
			ACTIVE_EVENT_PRIMARY_COLOR = context.getResources().getColor(R.color.primary_text);
		}
	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		// Information about group object must be displayed.
		// Object groupCursor cannot be placed by itself (all data from groupCursor -
		// data for other cursors - will be displayed), therefore clone
		// object must be created and returned.
		return CursorHelper.clone(groupCursor, 1);
	}

	@Override
	protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.item_event, parent, false);
	}

	@Override
	protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
		Event event = CursorHelper.extractEvent(cursor);
		TextView titleView = (TextView) view.findViewById(R.id.item_event_title);
		titleView.setText(event.getTitle());
		titleView.setTextColor(getColorFor(event, true));

		TextView descriptionView = (TextView) view.findViewById(R.id.item_event_description);
		descriptionView.setText(event.getDescription());
		descriptionView.setTextColor(getColorFor(event, true));

		ImageView imageView = (ImageView) view.findViewById(R.id.item_event_alarm);
		if (event.isActive(Calendar.getInstance())) {
			imageView.setImageDrawable(context.getDrawable(R.drawable.ic_alarm_on_black_48dp));
		} else {
			imageView.setImageDrawable(context.getDrawable(R.drawable.ic_alarm_off_black_48dp));
		}
		imageView.getDrawable().setTint(getColorFor(event, true));
		imageView.setOnClickListener(v -> {
			EventsSource eventsSource = new EventsSource(context, event.getCalendarId());
			if (event.isHasAlarms()) {
				eventsSource.disable(event.getId());
			} else {
				eventsSource.enable(event.getId());
			}
		});

		TextView textView = (TextView) view.findViewById(R.id.item_event_time);
		textView.setText(getTimePeriod(context, event));
		textView.setTextColor(getColorFor(event, false));
	}

	private String getTimePeriod(Context context, Event event) {
		if (event.getStartTime() != null && event.getEndTime() != null) {
			return MessageFormat.format("{0} - {1}", getFormattedTime(context, event.getStartTime()), getFormattedTime(context, event.getEndTime()));
		} else if (event.getStartTime() != null) {
			return MessageFormat.format("{0}", getFormattedTime(context, event.getStartTime()));
		} else if (event.getEndTime() != null) {
			return MessageFormat.format("now - {0}", getFormattedTime(context, event.getEndTime()));
		} else {
			return null;
		}
	}

	private String getFormattedTime(Context context, Calendar startTime) {
		return new SimpleDateFormat("d, LLL yyyy", context.getResources().getConfiguration().locale).format(new Date(startTime.getTimeInMillis()));
	}

	@Override
	protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.item_event_expanded, parent, false);
	}

	private static final ImmutableMap<Integer, StringValueExtractor> EVENT_VIEW_MAPPING;
	static {
		EVENT_VIEW_MAPPING = ImmutableMap.<Integer, StringValueExtractor>builder()
				.put(R.id.item_event_expanded_description, (event, context) -> event.getDescription())
				.put(R.id.item_event_expanded_location, (event, context) -> prettyFormat(event.getCoordinates()))
				.put(R.id.item_event_expanded_start_time, (event, context) -> getFormattedTimeString(R.string.main_event_element_start_time, event.getStartTime(), context))
				.put(R.id.item_event_expanded_end_time, (event, context) -> getFormattedTimeString(R.string.main_event_element_end_time, event.getEndTime(), context))
				.build();
	}

	private static String getFormattedTimeString(int resource, Calendar time, Context context) {
		return time != null ? format(context.getString(resource), formatDateTime(context, time)) : null;
	}

	private static Function<Event, String> createExtractor(final StringValueExtractor extractor, final Context context) {
		return (event) -> extractor.getString(event, context);
	}

	@Override
	protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
		Event event = CursorHelper.extractEvent(cursor);

		view.setOnClickListener(v -> {
			Intent intent = new Intent(context, MakeTaskActivity.class);
			intent.putExtra(MakeTaskActivity.INTENT_EDIT_TASK, event.getId());
			context.startActivity(intent);
		});

		for (Map.Entry<Integer, StringValueExtractor> viewMapping : EVENT_VIEW_MAPPING.entrySet()) {
			setElementValue(view, viewMapping.getKey(), event, createExtractor(viewMapping.getValue(), context));
		}
	}

	private int getColorFor(Event event, boolean isPrimary) {
		if (event.isActive(Calendar.getInstance())) {
			return isPrimary ? ACTIVE_EVENT_PRIMARY_COLOR : ACTIVE_EVENT_COLOUR;
		} else {
			return INACTIVE_EVENT_COLOUR;
		}
	}

	public void setElementValue(View parentView, int elementId, Event event, Function<Event, String> valueGetter) {
		TextView view = (TextView)parentView.findViewById(elementId);
		String text = valueGetter.apply(event);
		if (text == null || text.isEmpty()) {
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
			view.setText(text);
			view.setTextColor(getColorFor(event, true));
		}
	}
}