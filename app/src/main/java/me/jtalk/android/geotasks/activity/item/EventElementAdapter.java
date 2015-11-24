package me.jtalk.android.geotasks.activity.item;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.TextView;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.CursorHelper;
import me.jtalk.android.geotasks.util.TimeFormat;

public class EventElementAdapter extends CursorTreeAdapter {
	public EventElementAdapter(Context context) {
		super(null, context, true);
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
		TextView titleView = (TextView) view.findViewById(R.id.event_element_title);
		TextView timeView = (TextView) view.findViewById(R.id.event_element_time);
		TextView locationView = (TextView) view.findViewById(R.id.event_element_location);

		Event event = EventsSource.extractEvent(cursor);
		titleView.setText(event.getTitle());
		timeView.setText(TimeFormat.formatDateTime(context, event.getStartTime()));
		locationView.setText(event.getLocationText());
	}

	@Override
	protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.item_event_expanded, parent, false);
	}

	@Override
	protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
		TextView descriptionView = (TextView) view.findViewById(R.id.item_event_expanded_descripion);

		Event event = EventsSource.extractEvent(cursor);
		descriptionView.setText(event.getDescription());
	}
}