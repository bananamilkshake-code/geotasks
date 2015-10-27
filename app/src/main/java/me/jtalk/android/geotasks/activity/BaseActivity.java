package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import java.text.MessageFormat;

import me.jtalk.android.geotasks.application.GeoTasksApplication;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.PermissionDependantTask;
import me.jtalk.android.geotasks.util.TasksChain;

public abstract class BaseActivity extends Activity {
	private static final String TAG = BaseActivity.class.getName();

	protected EventsSource getEventsSource() {
		return ((GeoTasksApplication) getApplication()).getEventsSource();
	}

	protected void setEventsSource(EventsSource eventsSource) {
		((GeoTasksApplication) getApplication()).setEventsSource(eventsSource);
	}

	protected void onNeededPermissionDenied() {
	}

	protected PermissionDependantTask makeTask(Runnable r, String... permissions) {
		return new PermissionDependantTask(permissions) {
			@Override
			public void process() throws Exception {
				r.run();
			}
		};
	}

	protected void processChain(TasksChain<PermissionDependantTask> toProceed) {
		processChain(toProceed, toProceed.getCurrentTaskId());
	}

	protected void processChain(TasksChain<PermissionDependantTask> toProceed, int firstTask) {
		try {
			toProceed.startProcessingFrom(firstTask);
		} catch (SecurityException exception) {
			int currentTaskId = toProceed.getCurrentTaskId();
			Log.d(TAG, MessageFormat.format("No permissions for processing step {0}", currentTaskId));

			PermissionDependantTask currentTask = toProceed.getCurrentTask();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(currentTask.getNeededPermissions(), currentTaskId);
			} else {
				onNeededPermissionDenied();
			}
		} catch (Exception exception) {
			Log.w(TAG, MessageFormat.format("Unexpected exception during chain procession on task {0}", toProceed.getCurrentTaskId()));
			throw new RuntimeException(exception);
		}
	}

	protected void processPermissionRequestResult(TasksChain<PermissionDependantTask> addEventChain, int requestCode,
												  String[] permissions, int[] values) {
		if (permissions.length == 0 || values.length == 0) {
			// interrupted by user
			Log.d(TAG, "Permission request was interrupted by user");
			return;
		}

		PermissionDependantTask interrupted = addEventChain.getTask(requestCode);
		if (interrupted.checkGranted(permissions, values)) {
			processChain(addEventChain, requestCode);
		} else {
			onNeededPermissionDenied();
		}
	}
}
