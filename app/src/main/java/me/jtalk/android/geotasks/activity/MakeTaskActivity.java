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

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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

import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.application.service.Permission;
import me.jtalk.android.geotasks.application.service.PermissionAwareRunner;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.CoordinatesFormat;
import me.jtalk.android.geotasks.util.Logger;
import me.jtalk.android.geotasks.util.TextUpdater;
import me.jtalk.android.geotasks.util.TimeFormat;

public class MakeTaskActivity extends BaseActivity implements Validator.ValidationListener {
	private static final Logger LOG = new Logger(MakeTaskActivity.class);

	public static final String INTENT_EDIT_TASK = "edit";

	@NotEmpty(messageResId = R.string.make_task_error_title_is_empty)
	@Bind(R.id.make_task_name)
	EditText titleText;

	@Bind(R.id.make_task_description)
	EditText descriptionText;

	@Bind(R.id.make_task_location)
	TextView locationText;

	@Bind(R.id.make_task_start_date)
	TextView startDateText;

	@Bind(R.id.make_task_start_time)
	TextView startTimeText;

	@Bind(R.id.make_task_end_date)
	TextView endDateText;

	@Bind(R.id.make_task_end_time)
	TextView endTimeText;

	private Validator validator;

	private boolean isNewEvent;
	private Event event;

	private final PermissionAwareRunner permissionAwareRunner = new PermissionAwareRunner(this, this::noPermission);

	private void noPermission(Permission permission) {
		Toast.makeText(this, R.string.make_task_toast_event_creation_no_permission, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_task);

		ButterKnife.bind(this);

		validator = new Validator(this);
		validator.setValidationListener(this);

		long eventId = getIntent().getLongExtra(INTENT_EDIT_TASK, EventsSource.NO_TASK);
		isNewEvent = eventId == EventsSource.NO_TASK;
		if (isNewEvent) {
			event = Event.createEmpty();
		} else {
			LOG.debug("MakeTaskActivity has been opened to edit event {0}", eventId);
			event = Event.copyOf(getEventsSource().get(eventId));
			titleText.setText(event.getTitle());
			descriptionText.setText(event.getDescription());
			setLocationText();
			setTimeViews();
		}

		titleText.addTextChangedListener(new TextUpdater((value) -> event.setTitle(value)));
		descriptionText.addTextChangedListener(new TextUpdater((value) -> event.setDescription(value)));
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
		permissionAwareRunner.onPermissionUpdate(permissions, values, requestCode);
	}

	@Override
	public void onValidationSucceeded() {
		if (isNewEvent) {
			permissionAwareRunner.withPermissions(Permission.MANAGE_CALENDAR, this::addEvent);
		} else {
			permissionAwareRunner.withPermissions(Permission.MANAGE_CALENDAR, this::editEvent);
		}
	}

	@Override
	public void onValidationFailed(List<ValidationError> errors) {
		for (ValidationError error : errors) {
			View view = error.getView();
			String message = error.getCollatedErrorMessage(this);

			if (view instanceof EditText) {
				((EditText) view).setError(message);
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
	 * This method is called on activity_make_task.add_event_location_pick_button.
	 *
	 * @param view
	 */
	public void showLocationActivity(View view) {
		permissionAwareRunner.withPermissions(Permission.PICK_LOCATION, this::openLocationPickActivity);
	}

	/**
	 * This method is called on activity_make_task.add_event_time_text click.
	 *
	 * @param view
	 */
	public void showTimePickerDialog(View view) {
		final Calendar calendar = getTimeByView(view);
		TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);
			setTimeByView(view, calendar);
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
		timePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.make_task_dialog_button_time_clear), (dialog, which) -> setTimeByView(view, null));
		timePickerDialog.show();
	}

	/**
	 * This method is called on activity_make_task.add_event_date_text click.
	 *
	 * @param view
	 */
	public void showDatePickerDialog(View view) {
		final Calendar calendar = getTimeByView(view);
		DatePickerDialog datePickerDialog = new DatePickerDialog(this, (dateView, year, monthOfYear, dayOfMonth) -> {
			calendar.set(year, monthOfYear, dayOfMonth);
			setTimeByView(view, calendar);
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.make_task_dialog_button_time_clear), (dialog, which) -> setTimeByView(view, null));
		datePickerDialog.show();
	}

	private void setLocationText() {
		locationText.setText(CoordinatesFormat.prettyFormat(event.getCoordinates()));
	}

	private void setTimeViews() {
		setDateForView(startDateText, startTimeText, event.getStartTime());
		setDateForView(endDateText, endTimeText, event.getEndTime());
	}

	private void setDateForView(TextView dateView, TextView timeView, Calendar calendar) {
		dateView.setText(TimeFormat.formatDate(this, calendar));
		timeView.setText(TimeFormat.formatTime(this, calendar));
	}

	private boolean isStartTimeView(View view) {
		return view == startDateText || view == startTimeText;
	}

	private boolean isEndTimeView(View view) {
		return view == endDateText || view == endTimeText;
	}

	/**
	 * Return value of event time that is bound to given view:
	 * date
	 * @param view to get time for
	 * @return
	 */
	private Calendar getTimeByView(View view) {
		Calendar calendar = null;
		if (isStartTimeView(view)) {
			calendar = event.getStartTime();
		} else if (isEndTimeView(view)) {
			calendar = event.getEndTime();
		} else {
			LOG.warn("Incorrect view to get calendar instance");
		}

		if (calendar == null) {
			calendar = Calendar.getInstance();
		}
		return calendar;
	}

	private void setTimeByView(View view, Calendar calendar) {
		// calendar can be newly created, so we need to set it manually
		if (isStartTimeView(view)) {
			event.setStartTime(calendar);
		} else if (isEndTimeView(view)) {
			event.setEndTime(calendar);
		} else {
			LOG.warn("Incorrect view to set date, nothing changed");
			return;
		}
		setTimeViews();
	}

	private void onLocationResult(Intent data) {
		event.setCoordinates(data.getParcelableExtra(LocationPickActivity.INTENT_EXTRA_COORDINATES));
		setLocationText();
	}

	private void addEvent() {
		getEventsSource().add(event);
		finish();
	}

	private void editEvent() {
		getEventsSource().edit(event);
		finish();
	}

	private void openLocationPickActivity() {
		Intent intent = new Intent(this, LocationPickActivity.class);
		final TaskCoordinates coordinates = event.getCoordinates();
		if (coordinates != null) {
			intent.putExtra(LocationPickActivity.INTENT_EXTRA_EDIT, true);
			intent.putExtra(LocationPickActivity.INTENT_EXTRA_COORDINATES, coordinates);
		}

		startActivityForResult(intent, LocationPickActivity.INTENT_LOCATION_PICK);
	}
}