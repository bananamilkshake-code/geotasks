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

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import me.jtalk.android.geotasks.application.TaskChainHandler;
import me.jtalk.android.geotasks.location.TaskCoordinates;
import me.jtalk.android.geotasks.source.Event;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.CoordinatesFormat;
import me.jtalk.android.geotasks.util.Logger;
import me.jtalk.android.geotasks.util.PermissionDependentTask;
import me.jtalk.android.geotasks.util.TasksChain;
import me.jtalk.android.geotasks.util.TimeFormat;

import static me.jtalk.android.geotasks.application.TaskChainHandler.makeTask;

public class MakeTaskActivity extends BaseActivity implements Validator.ValidationListener {
	private static final Logger LOG = new Logger(MakeTaskActivity.class);

	public static final String INTENT_EDIT_TASK = "edit";

	@NotEmpty
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

	private int saveEventChainId;
	private int openLocationPickActivityChainId;

	private Event event;

	private TaskChainHandler chainHandler = new TaskChainHandler(this) {
		@Override
		protected void onNeededPermissionDenied() {
			Toast.makeText(MakeTaskActivity.this, R.string.make_task_toast_event_creation_no_permission, Toast.LENGTH_LONG).show();
		}
	};

	{
		openLocationPickActivityChainId = chainHandler.addTaskChain(new TasksChain<PermissionDependentTask>()
				.add(makeTask(this::openLocationPickActivity,
						Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
						Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
						Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE)));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_task);

		ButterKnife.bind(this);

		validator = new Validator(this);
		validator.setValidationListener(this);

		long eventId = getIntent().getLongExtra(INTENT_EDIT_TASK, EventsSource.NO_TASK);
		if (eventId == EventsSource.NO_TASK) {
			event = Event.createEmpty();
			saveEventChainId = chainHandler.addTaskChain(new TasksChain<PermissionDependentTask>()
					.add(makeTask(this::addEvent, Manifest.permission.WRITE_CALENDAR)));
		} else {
			LOG.debug("MakeTaskActivity has been opened to edit event {0}", eventId);

			event = Event.copyOf(getEventsSource().get(eventId));

			titleText.setText(event.getTitle());
			descriptionText.setText(event.getDescription());
			setLocationText();
			setTimeViews();

			saveEventChainId = chainHandler.addTaskChain(new TasksChain<PermissionDependentTask>()
					.add(makeTask(this::editEvent, Manifest.permission.WRITE_CALENDAR)));
		}

		titleText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				event.setTitle(s.toString());
			}
		});
		descriptionText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				event.setDescription(s.toString());
			}
		});
	}

	private void setLocationText() {
		locationText.setText(CoordinatesFormat.prettyFormat(event.getCoordinates()));
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
		chainHandler.processPermissionRequestResult(requestCode, permissions, values);
	}

	@Override
	public void onValidationSucceeded() {
		chainHandler.processChain(saveEventChainId);
	}

	@Override
	public void onValidationFailed(List<ValidationError> errors) {
		for (ValidationError validationError : errors) {
			View view = validationError.getView();

			if (view == titleText) {
				titleText.setError(getString(R.string.make_task_error_title_is_empty));
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
		chainHandler.processChain(openLocationPickActivityChainId);
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