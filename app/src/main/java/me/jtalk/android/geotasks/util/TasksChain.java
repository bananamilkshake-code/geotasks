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

import java.util.ArrayList;
import java.util.List;

import static me.jtalk.android.geotasks.util.Assert.verifyArgument;

public class TasksChain <T extends TasksChain.Task> {
	private boolean pause;

	public static abstract class Task {
		public abstract void process() throws Exception;
	}

	private List<T> tasks;

	private int currentTask;

	public TasksChain() {
		tasks = new ArrayList<>();
	}

	/**
	 * Adds new task to be proceeded at the end of the chain.
	 *
	 * @param task Task to add. Cannot be null.
	 * @return
	 */
	public TasksChain<T> add(T task) {
		verifyArgument(task != null, "Task cannot be null");

		tasks.add(task);
		return this;
	}

	public void startProcessing() throws Exception {
		pause = false;
		while (!pause && currentTask < tasks.size()) {
			try {
				tasks.get(currentTask).process();

				++currentTask;
			} catch (Exception exception) {
				throw exception;
			}
		}

		if (!pause) {
			reset();
		}
	}

	public void startProcessingFrom(int taskId) throws Exception {
		this.currentTask = taskId;
		startProcessing();
	}

	public void pause() {
		pause = true;
	}

	public int getCurrentTaskId() {
		return currentTask;
	}

	public T getCurrentTask() {
		return tasks.get(currentTask);
	}

	public T getTask(int taskId) {
		return tasks.get(taskId);
	}

	public void reset() {
		currentTask = 0;
	}
}
