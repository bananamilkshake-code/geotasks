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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.Settings;


public class AddEventActivity extends BaseActivity {
	private static final String TAG = AddEventActivity.class.getName();

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

	private static final int PERMISSION_REQUEST_WRITE_CALENDAR = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_add_event, menu);
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] values) {
		if (permissions.length == 0 || values.length == 0) {
			// interrupted by user
			Log.d(TAG, "Permission request was interrupted by user");
			return;
		}

		switch (requestCode) {
			case PERMISSION_REQUEST_WRITE_CALENDAR:
				onWriteCalendarPermissionGranted(permissions, values);
				return;
		}
	}

	private void onWriteCalendarPermissionGranted(String[] permissions, int[] values) {
		if (checkGranted(Manifest.permission.WRITE_CALENDAR, permissions, values)) {
			addCalendar();
		}else {
			onNoPermissionWarning();
		}
	}

	// This method is called on menu_add_event.menu_action_add_event_save click
	public void onAddCalendarClick(MenuItem menuItem) {
		if (!isPermissionGranted(Manifest.permission.WRITE_CALENDAR, PERMISSION_REQUEST_WRITE_CALENDAR)) {
			return;
		}

		addCalendar();
	}

	private void addCalendar() {
		TextView titleText = (TextView) findViewById(R.id.add_event_name_text);
		TextView descriptionText = (TextView) findViewById(R.id.add_event_description_text);

		String eventTitle = titleText.getText().toString();
		String eventDescription = descriptionText.getText().toString();
		long startTime = this.getStartTime();
		long endTime = Settings.DEFAULT_END_TIME;

		getEventsSource().addEvent(eventTitle, eventDescription, startTime, endTime);

		finish();
	}

	private long getStartTime() {
		Calendar dateCalendar = parseFromTextView(R.id.add_event_date_text, DATE_FORMAT);
		Calendar timeCalendar = parseFromTextView(R.id.add_event_time_text, TIME_FORMAT);

		if (dateCalendar == null || timeCalendar == null) {
			return Settings.DEFAULT_START_TIME;
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
			Log.w(TAG, String.format("Parsing event start time values %s from view %i failed", calendarStr, viewId), exception);
			return null;
		}

		return calendar;
	}

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

	private void onNoPermissionWarning() {
		Toast.makeText(this, R.string.toast_event_creation_no_permission, Toast.LENGTH_LONG).show();
	}
}
