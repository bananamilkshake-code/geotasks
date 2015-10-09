package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.source.CalendarSource;

public class MainActivity extends Activity {

	private static final int LOADER_CALENDARS_ID = 0;

	private static final int INTENT_ADD_CALENDAR = 0;

	CalendarSource calendarSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SimpleCursorAdapter calendarsAdapter = initCalendarsList();

		calendarSource = new CalendarSource(getApplicationContext(), calendarsAdapter);

		getLoaderManager().initLoader(LOADER_CALENDARS_ID, null, calendarSource);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		menu.findItem(R.id.actionAddCalendar).setOnMenuItemClickListener(item -> {
			startActivityForResult(new Intent(MainActivity.this, AddCalendarActivity.class), INTENT_ADD_CALENDAR);
			return true;
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		
		switch (requestCode) {
			case INTENT_ADD_CALENDAR:
				onAddCalendarResult(data);
				return;
		}
	}

	private void onAddCalendarResult(Intent data) {
		String name = data.getStringExtra(AddCalendarActivity.EXTRA_NAME);
		calendarSource.addCalendar(name);
	}

	private SimpleCursorAdapter initCalendarsList() {
		SimpleCursorAdapter calendarsAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, null,
				new String[] {CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars._ID},
				new int[] {android.R.id.text1, android.R.id.text2}, 0);

		ListView calendarsList = (ListView) findViewById(R.id.calendars_list);
		calendarsList.setAdapter(calendarsAdapter);
		calendarsList.setOnItemClickListener((parent, view, position, id) -> {
            Cursor item = (Cursor) parent.getItemAtPosition(position);
            int calendarId = item.getInt(item.getColumnIndex(CalendarContract.Calendars._ID));
            Intent eventsIntent = new Intent(MainActivity.this, EventsActivity.class);
            eventsIntent.putExtra(EventsActivity.INTENT_EXTRA_CALENDAR_ID, calendarId);
			startActivity(eventsIntent);
        });

		return calendarsAdapter;
	}
}
