package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import me.jtalk.android.geotasks.application.GeoTasksApplication;
import me.jtalk.android.geotasks.source.EventsSource;
import me.jtalk.android.geotasks.util.Logger;
import me.jtalk.android.geotasks.util.PermissionDependentTask;
import me.jtalk.android.geotasks.util.TasksChain;

public abstract class BaseActivity extends Activity {
	private static final Logger LOG = new Logger(BaseActivity.class);

	private List<TasksChain<PermissionDependentTask>> chains = new ArrayList<>();

	protected int addTaskChain(@NonNull TasksChain<PermissionDependentTask> chain) {
		chains.add(chain);
		return chains.size() - 1;
	}

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

	protected void processChain(int chainId) {
		processChain(chainId, chains.get(chainId).getCurrentTaskId());
	}

	protected void processChain(int chainId, int firstTask) {
		TasksChain<PermissionDependentTask> toProceed = chains.get(chainId);

		try {
			toProceed.startProcessingFrom(firstTask);
		} catch (SecurityException exception) {
			int currentTaskId = toProceed.getCurrentTaskId();

			LOG.debug("No permissions for processing task chain {0} step {1}", chainId, currentTaskId);

			PermissionDependentTask currentTask = toProceed.getCurrentTask();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(currentTask.getNeededPermissions(), chainId);
			} else {
				onNeededPermissionDenied();
			}
		} catch (Exception exception) {
			LOG.warn("Unexpected exception during chain {0} procession on step {1}", chainId, toProceed.getCurrentTaskId());
			throw new RuntimeException(exception);
		}
	}

	protected void processPermissionRequestResult(int requestCode, String[] permissions, int[] values) {
		if (permissions.length == 0 || values.length == 0) {
			// interrupted by user
			LOG.debug("Permission request was interrupted by user");
			return;
		}

		TasksChain<PermissionDependentTask> chain = chains.get(requestCode);

		PermissionDependentTask interrupted = chain.getCurrentTask();
		if (interrupted.checkGranted(permissions, values)) {
			processChain(requestCode);
		} else {
			onNeededPermissionDenied();
		}
	}
}
