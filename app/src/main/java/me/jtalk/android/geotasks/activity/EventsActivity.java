package me.jtalk.android.geotasks.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.provider.CalendarContract;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.calendar.EventsSource;

public class EventsActivity extends Activity {

    private static final int LOADER_EVENTS_ID = 0;

    public static final String INTENT_EXTRA_CALENDAR_ID = "calendar-id";

    private EventsSource eventsSource;
    private int calendarId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SimpleCursorAdapter eventsAdapter = initEventsList();

        eventsSource = new EventsSource(this, eventsAdapter);

        calendarId = getIntent().getIntExtra(INTENT_EXTRA_CALENDAR_ID, -1);

        Bundle bundle = new Bundle();
        bundle.putInt(EventsSource.BUNDLE_CALENDAR_ID, calendarId);

        getLoaderManager().initLoader(LOADER_EVENTS_ID, bundle, eventsSource);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_events, menu);

        menu.findItem(R.id.actionAddEvent).setOnMenuItemClickListener(item -> {
            startActivity(new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.CALENDAR_ID, calendarId));
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    private SimpleCursorAdapter initEventsList() {
        SimpleCursorAdapter eventsAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null,
                new String[] {CalendarContract.Events.TITLE},
                new int[] {android.R.id.text1}, 0);

        ListView eventsList = (ListView) findViewById(R.id.eventsList);
        eventsList.setAdapter(eventsAdapter);

        return eventsAdapter;
    }
}
