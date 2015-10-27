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
