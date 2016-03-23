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
package me.jtalk.android.geotasks.util;

import android.content.pm.PackageManager;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class PermissionDependentTask extends TasksChain.Task {
	private static final Logger LOG = new Logger(PermissionDependentTask.class);

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