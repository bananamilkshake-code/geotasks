package me.jtalk.android.geotasks.activity.item;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;

public class EventElementAdapter extends CursorAdapter {
	public EventElementAdapter(Context context) {
		super(context, null, true);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.item_event, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView titleView = (TextView) view.findViewById(R.id.event_element_title);
		TextView timeView = (TextView) view.findViewById(R.id.event_element_time);
		TextView locationView = (TextView) view.findViewById(R.id.event_element_location);

		Event event = EventsSource.extractEvent(cursor);
		titleView.setText(event.getTitle());
		timeView.setText(event.getStartTimeText());
		locationView.setText(event.getLocationText());
	}
}