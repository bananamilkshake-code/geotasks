package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.os.Build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.jtalk.android.geotasks.application.GeoTasksApplication;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.PermissionDependentTask;
import me.jtalk.android.geotasks.util.TasksChain;

public abstract class BaseActivity extends Activity {
	private static final Logger LOG = LoggerFactory.getLogger(BaseActivity.class);

	protected EventsSource getEventsSource() {
		return ((GeoTasksApplication) getApplication()).getEventsSource();
	}

	protected void setEventsSource(EventsSource eventsSource) {
		((GeoTasksApplication) getApplication()).setEventsSource(eventsSource);
	}

	protected void onNeededPermissionDenied() {
	}

	protected PermissionDependentTask makeTask(Runnable r, String... permissions) {
		return new PermissionDependentTask(permissions) {
			@Override
			public void process() throws Exception {
				r.run();
			}
		};
	}

	protected void processChain(TasksChain<PermissionDependentTask> toProceed) {
		processChain(toProceed, toProceed.getCurrentTaskId());
	}

	protected void processChain(TasksChain<PermissionDependentTask> toProceed, int firstTask) {
		try {
			toProceed.startProcessingFrom(firstTask);
		} catch (SecurityException exception) {
			int currentTaskId = toProceed.getCurrentTaskId();
			LOG.debug("No permissions for processing step {}", currentTaskId);

			PermissionDependentTask currentTask = toProceed.getCurrentTask();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(currentTask.getNeededPermissions(), currentTaskId);
			} else {
				onNeededPermissionDenied();
			}
		} catch (Exception exception) {
			LOG.warn("Unexpected exception during chain procession on task {}", toProceed.getCurrentTaskId());
			throw new RuntimeException(exception);
		}
	}

	protected void processPermissionRequestResult(TasksChain<PermissionDependentTask> addEventChain, int requestCode,
												  String[] permissions, int[] values) {
		if (permissions.length == 0 || values.length == 0) {
			// interrupted by user
			LOG.debug("Permission request was interrupted by user");
			return;
		}

		PermissionDependentTask interrupted = addEventChain.getTask(requestCode);
		if (interrupted.checkGranted(permissions, values)) {
			processChain(addEventChain, requestCode);
		} else {
			onNeededPermissionDenied();
		}
	}
}
