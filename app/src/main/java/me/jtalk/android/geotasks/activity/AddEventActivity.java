package me.jtalk.android.geotasks.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.Settings;

public class AddEventActivity extends Activity {
	public static final String EXTRA_TITLE = "event-name";
	public static final String EXTRA_DESCRIPTION = "event-description";
	public static final String EXTRA_START_TIME = "event-dtstart";
	public static final String EXTRA_END_TIME = "event-dtend";

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

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

	// This method is called on menu_add_event.menu_action_add_event_save click
	public void onAddCalendarClick(MenuItem menuItem) throws ParseException {
		TextView nameText = (TextView) findViewById(R.id.add_event_name_text);
		TextView descriptionText = (TextView) findViewById(R.id.add_event_description_text);

		Intent returnIntent = new Intent(this, MainActivity.class);
		returnIntent.putExtra(EXTRA_TITLE, nameText.getText().toString());
		returnIntent.putExtra(EXTRA_DESCRIPTION, descriptionText.getText().toString());
		returnIntent.putExtra(EXTRA_START_TIME, this.getStartTime());
		setResult(RESULT_OK, returnIntent);

		finish();
	}

	private long getStartTime() throws ParseException {
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

	private Calendar parseFromTextView(int viewId, DateFormat format) throws ParseException {
		String calendarStr = ((TextView) findViewById(viewId)).getText().toString();
		if (calendarStr.isEmpty()) {
			return null;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(format.parse(calendarStr.toString()));
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
}
