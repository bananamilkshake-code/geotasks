package me.jtalk.android.geotasks.util;

import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class PermissionDependantTasksChain extends TasksChain <PermissionDependantTasksChain.PermissionDependantTask> {
	private static final String TAG = PermissionDependantTask.class.getName();

	public abstract class PermissionDependantTask extends TasksChain.Task {
		private String[] neededPermissions;

		public PermissionDependantTask(String[] neededPermissions) {
			this.neededPermissions = neededPermissions;
		}

		public boolean checkGranted(String[] permissions, int[] values) {
			if (permissions.length == 0 || values.length == 0) {
				Log.w(TAG, "Permissions request was interrupted by user");
				return false;
			}

			for (String permission : neededPermissions) {
				List<String> permissionsList = Arrays.asList(permissions);
				if (!permissionsList.contains(permission)) {
					return false;
				}

				if (values[permissionsList.indexOf(permission)] != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}

			return true;
		}

		public String[] getNeededPermissions() {
			return neededPermissions;
		}
	}
}