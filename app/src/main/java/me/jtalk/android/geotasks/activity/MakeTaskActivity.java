package me.jtalk.android.geotasks.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.CoordinatesFormat;
import me.jtalk.android.geotasks.util.Logger;
import me.jtalk.android.geotasks.util.PermissionDependentTask;
import me.jtalk.android.geotasks.util.TasksChain;
import me.jtalk.android.geotasks.util.TimeFormat;

public class MakeTaskActivity extends BaseActivity implements Validator.ValidationListener {
	private static final Logger LOG = new Logger(MakeTaskActivity.class);

	public static final String INTENT_EDIT_TASK = "edit";

	private static final long NO_TASK = -1;

	@NotEmpty
	@Bind(R.id.add_event_name_text)
	EditText titleText;

	@Bind(R.id.add_event_description_text)
	EditText descriptionText;

	@Bind(R.id.add_event_location_coordinates_text)
	TextView locationText;

	@Bind(R.id.add_event_date_text)
	TextView dateText;

	@Bind(R.id.add_event_time_text)
	TextView timeText;

	private Validator validator;

	private TasksChain<PermissionDependentTask> saveEventChain;
	private TasksChain<PermissionDependentTask> openLocationPickActivityChain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);

		ButterKnife.bind(this);

		validator = new Validator(this);
		validator.setValidationListener(this);

		long eventId = getIntent().getLongExtra(INTENT_EDIT_TASK, NO_TASK);
		if (eventId == NO_TASK) {
			saveEventChain = new TasksChain<PermissionDependentTask>()
					.add(makeTask(this::addEvent, Manifest.permission.WRITE_CALENDAR));
		} else {
			LOG.debug("MakeTaskActivity has been opened to edit event {0}", eventId);

			Event event = getEventsSource().get(eventId);

			titleText.setText(event.getTitle());
			descriptionText.setText(event.getDescription());
			locationText.setText(CoordinatesFormat.formatSimple(event.getCoordinates()));

			saveEventChain = new TasksChain<PermissionDependentTask>()
					.add(makeTask(() -> editEvent(eventId), Manifest.permission.WRITE_CALENDAR));
		}

		openLocationPickActivityChain = new TasksChain<PermissionDependentTask>()
				.add(makeTask(this::openLocationPickActivity,
						Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
						Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
						Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_add_event, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
			case LocationPickActivity.INTENT_LOCATION_PICK:
				onLocationResult(data);
				break;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] values) {
		processPermissionRequestResult(saveEventChain, requestCode, permissions, values);
	}

	@Override
	protected void onNeededPermissionDenied() {
		Toast.makeText(this, R.string.toast_event_creation_no_permission, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onValidationSucceeded() {
		processChain(saveEventChain);
	}

	@Override
	public void onValidationFailed(List<ValidationError> errors) {
		for (ValidationError validationError : errors) {
			View view = validationError.getView();

			if (view == titleText) {
				titleText.setError(getString(R.string.add_event_title_text_error));
			}
		}
	}

	/**
	 * This method is called on menu_add_event.menu_action_add_event_save click.
	 *
	 * @param menuItem
	 */
	public void onAddCalendarClick(MenuItem menuItem) {
		validator.validate();
	}

	/**
	 * This method is called on activity_add_event.add_event_location_pick_button.
	 *
	 * @param view
	 */
	public void showLocationActivity(View view) {
		processChain(openLocationPickActivityChain);
	}

	/**
	 * This method is called on activity_add_event.add_event_time_text click.
	 *
	 * @param view
	 */
	public void showTimePickerDialog(View view) {
		TextView textView = (TextView) view;
		Calendar calendar = Calendar.getInstance();

		try {
			String timeText = textView.getText().toString();
			if (!timeText.isEmpty()) {
				calendar = TimeFormat.parseTime(this, timeText);
			}
		} catch (ParseException exception) {
			LOG.warn(exception, "Time value from time text field cannot be parsed");
		}

		new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
			Calendar picked = Calendar.getInstance();
			picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
			picked.set(Calendar.MINUTE, minute);
			textView.setText(TimeFormat.formatTime(this, picked));
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
	}

	/**
	 * This method is called on activity_add_event.add_event_date_text click.
	 *
	 * @param view
	 */
	public void showDatePickerDialog(View view) {
		TextView textView = (TextView) view;
		Calendar calendar = Calendar.getInstance();

		try {
			String dateText = textView.getText().toString();
			if (!dateText.isEmpty()) {
				calendar = (TimeFormat.parseDate(this, dateText));
			}
		} catch (ParseException exception) {
			LOG.warn(exception, "Date value from date text field cannot be parsed");
		}

		new DatePickerDialog(this, (dateView, year, monthOfYear, dayOfMonth) -> {
			Calendar picked = Calendar.getInstance();
			picked.set(year, monthOfYear, dayOfMonth);
			textView.setText(TimeFormat.formatDate(this, picked));
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
	}

	private void onLocationResult(Intent data) {
		TaskCoordinates taskCoordinates = data.getParcelableExtra(LocationPickActivity.INTENT_EXTRA_COORDINATES);
		locationText.setText(CoordinatesFormat.formatSimple(taskCoordinates));
	}

	private void addEvent() {
		String eventTitle = titleText.getText().toString();
		String eventDescription = descriptionText.getText().toString();
		String location = locationText.getText().toString();
		Calendar startTime = this.getStartTime();
		Calendar endTime = EventsSource.EMPTY_TIME;

		getEventsSource().add(eventTitle, eventDescription, location, startTime, endTime);

		finish();
	}

	private void editEvent(long eventId) {
		String eventTitle = titleText.getText().toString();
		String eventDescription = descriptionText.getText().toString();
		String location = locationText.getText().toString();
		Calendar startTime = this.getStartTime();
		Calendar endTime = EventsSource.EMPTY_TIME;

		getEventsSource().edit(eventId, eventTitle, eventDescription, location, startTime, endTime);

		finish();
	}

	private void openLocationPickActivity() {
		Intent intent = new Intent(this, LocationPickActivity.class);
		String locationString = locationText.getText().toString();
		if (!locationString.isEmpty()) {
			intent.putExtra(LocationPickActivity.INTENT_EXTRA_EDIT, true);
			intent.putExtra(LocationPickActivity.INTENT_EXTRA_COORDINATES, CoordinatesFormat.parse(locationString));
		}

		startActivityForResult(intent, LocationPickActivity.INTENT_LOCATION_PICK);
	}

	private Calendar getStartTime() {
		Calendar dateCalendar = parseFromTextView(dateText, TimeFormat.getDateFormat(this));
		Calendar timeCalendar = parseFromTextView(timeText, TimeFormat.getTimeFormat(this));

		if (dateCalendar == null || timeCalendar == null) {
			return EventsSource.EMPTY_TIME;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.set(dateCalendar.get(Calendar.YEAR),
				dateCalendar.get(Calendar.MONTH),
				dateCalendar.get(Calendar.DAY_OF_MONTH),
				timeCalendar.get(Calendar.HOUR_OF_DAY),
				timeCalendar.get(Calendar.MINUTE));
		return calendar;
	}

	private Calendar parseFromTextView(TextView view, DateFormat format) {
		String calendarStr = view.getText().toString();
		if (calendarStr.isEmpty()) {
			return null;
		}

		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(format.parse(calendarStr));
			return calendar;
		} catch (ParseException exception) {
			LOG.warn(exception, "Parsing event start time values {0} from view {1} failed", calendarStr, view.getId());
			return null;
		}
	}
}