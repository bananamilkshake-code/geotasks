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
package me.jtalk.android.geotasks.application;

import android.app.Activity;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import me.jtalk.android.geotasks.util.Logger;
import me.jtalk.android.geotasks.util.PermissionDependentTask;
import me.jtalk.android.geotasks.util.TasksChain;

public abstract class TaskChainHandler {

	private static final Logger LOG = new Logger(TaskChainHandler.class);

	private Activity owner;

	private List<TasksChain<PermissionDependentTask>> chains = new ArrayList<>();

	static public PermissionDependentTask makeTask(Runnable r, String... permissions) {
		return new PermissionDependentTask(permissions) {
			@Override
			public void process() throws Exception {
				r.run();
			}
		};
	}

	public TaskChainHandler(Activity owner) {
		this.owner = owner;
	}

	public int addTaskChain(@NonNull TasksChain<PermissionDependentTask> chain) {
		chains.add(chain);
		return chains.size() - 1;
	}

	public void processChain(int chainId) {
		processChain(chainId, chains.get(chainId).getCurrentTaskId());
	}

	public void processPermissionRequestResult(int requestCode, String[] permissions, int[] values) {
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

	protected abstract void onNeededPermissionDenied();

	protected void processChain(int chainId, int firstTask) {
		TasksChain<PermissionDependentTask> toProceed = chains.get(chainId);

		try {
			toProceed.startProcessingFrom(firstTask);
		} catch (SecurityException exception) {
			int currentTaskId = toProceed.getCurrentTaskId();

			LOG.debug("No permissions for processing task chain {0} step {1}", chainId, currentTaskId);

			PermissionDependentTask currentTask = toProceed.getCurrentTask();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				owner.requestPermissions(currentTask.getNeededPermissions(), chainId);
			} else {
				onNeededPermissionDenied();
			}
		} catch (Exception exception) {
			LOG.warn("Unexpected exception during chain {0} procession on step {1}", chainId, toProceed.getCurrentTaskId());
			throw new RuntimeException(exception);
		}
	}
}
