package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.text.MessageFormat;

import me.jtalk.android.geotasks.application.GeoTasksApplication;
import me.jtalk.android.geotasks.R;
import me.jtalk.android.geotasks.source.EventsSource;

public abstract class BaseActivity extends Activity {
	private static final String TAG = BaseActivity.class.getName();

	protected EventsSource getEventsSource() {
		return ((GeoTasksApplication) getApplication()).getEventsSource();
	}

	protected void setEventsSource(EventsSource eventsSource) {
		((GeoTasksApplication) getApplication()).setEventsSource(eventsSource);
	}

	protected boolean isPermissionGranted(String permission, int requestId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{permission}, requestId);
				return false;
			} else {
				return true;
			}
		}

		return false;
	}

	protected void onNoPermissionError() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_no_permission_for_calendar_creation_title)
				.setMessage(R.string.dialog_no_permission_for_calendar_creation_message)
				.setPositiveButton(R.string.dialog_no_permission_for_calendar_creation_button, (dialog, which) -> {
					dialog.dismiss();
					BaseActivity.this.finish();
				})
				.show();
	}

	public static boolean checkGranted(String requestedPermission, String[] permissions, int[] values) {
		int permissionId = Arrays.asList(permissions).indexOf(requestedPermission);
		if (permissionId == -1) {
			Log.wtf(TAG, String.format("Permission %s was requested but not returned", requestedPermission));
			return false;
		}

		return values[permissionId] == PackageManager.PERMISSION_GRANTED;
	}
}
