package me.jtalk.android.geotasks.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.PermissionDependantTasksChain;


public class AddEventActivity extends BaseActivity {
	private static final String TAG = AddEventActivity.class.getName();

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

	PermissionDependantTasksChain addEventChain = new PermissionDependantTasksChain();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);

		addEventChain.addTask(addEventChain.new PermissionDependantTask(new String[]{Manifest.permission.WRITE_CALENDAR}) {
			@Override
			public void process() throws Exception {
				addCalendar();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_add_event, menu);
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] values) {
		processPermissionRequestResult(addEventChain, requestCode, permissions, values);
	}

	@Override
	protected void onNeededPermissionDenied() {
		Toast.makeText(this, R.string.toast_event_creation_no_permission, Toast.LENGTH_LONG).show();
	}

	// This method is called on menu_add_event.menu_action_add_event_save click
	public void onAddCalendarClick(MenuItem menuItem) {
		processChain(addEventChain);
	}

	// This method is called on activity_add_event.add_event_time_text click
	public void showTimePickerDialog(View view) throws ParseException {
		TextView textView = (TextView) view;
		Calendar calendar = Calendar.getInstance();

		String timeText = textView.getText().toString();
		if (!timeText.isEmpty()) {
			calendar.setTime(TIME_FORMAT.parse(timeText));
		}

		new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
			Calendar picked = Calendar.getInstance();
			picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
			picked.set(Calendar.MINUTE, minute);
			textView.setText(TIME_FORMAT.format(picked.getTime()));
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
	}

	// This method is called on activity_add_event.add_event_date_text click
	public void showDatePickerDialog(View view) throws ParseException {
		TextView textView = (TextView) view;
		Calendar calendar = Calendar.getInstance();

		String dateText = textView.getText().toString();
		if (!dateText.isEmpty()) {
			calendar.setTime(DATE_FORMAT.parse(dateText));
		}

		new DatePickerDialog(this, (dateView, year, monthOfYear, dayOfMonth) -> {
			Calendar picked = Calendar.getInstance();
			picked.set(year, monthOfYear, dayOfMonth);
			textView.setText(DATE_FORMAT.format(picked.getTime()));
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
	}

	private void addCalendar() {
		TextView titleText = (TextView) findViewById(R.id.add_event_name_text);
		TextView descriptionText = (TextView) findViewById(R.id.add_event_description_text);

		String eventTitle = titleText.getText().toString();
		String eventDescription = descriptionText.getText().toString();
		long startTime = this.getStartTime();
		long endTime = EventsSource.DEFAULT_END_TIME;

		getEventsSource().addEvent(eventTitle, eventDescription, startTime, endTime);

		finish();
	}

	private long getStartTime() {
		Calendar dateCalendar = parseFromTextView(R.id.add_event_date_text, DATE_FORMAT);
		Calendar timeCalendar = parseFromTextView(R.id.add_event_time_text, TIME_FORMAT);

		if (dateCalendar == null || timeCalendar == null) {
			return EventsSource.DEFAULT_START_TIME;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.set(dateCalendar.get(Calendar.YEAR),
				dateCalendar.get(Calendar.MONTH),
				dateCalendar.get(Calendar.DAY_OF_MONTH),
				timeCalendar.get(Calendar.HOUR_OF_DAY),
				timeCalendar.get(Calendar.MINUTE));
		return calendar.getTimeInMillis();
	}

	private Calendar parseFromTextView(int viewId, DateFormat format) {
		String calendarStr = ((TextView) findViewById(viewId)).getText().toString();
		if (calendarStr.isEmpty()) {
			return null;
		}

		Calendar calendar = Calendar.getInstance();

		try {
			calendar.setTime(format.parse(calendarStr));
		} catch (ParseException exception) {
			Log.w(TAG, MessageFormat.format("Parsing event start time values {0} from view {1} failed", calendarStr, viewId), exception);
			return null;
		}

		return calendar;
	}
}
