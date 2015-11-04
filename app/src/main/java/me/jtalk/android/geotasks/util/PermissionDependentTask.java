package me.jtalk.android.geotasks.util;

import android.content.pm.PackageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class PermissionDependentTask extends TasksChain.Task {
	private static final Logger LOG = LoggerFactory.getLogger(PermissionDependentTask.class);

	private String[] neededPermissions;

	public boolean checkGranted(String[] permissions, int[] values) {
		if (permissions.length == 0 || values.length == 0) {
			LOG.debug("Permissions request was interrupted by user");
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