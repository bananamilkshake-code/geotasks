package me.jtalk.android.geotasks.util;

import java.util.ArrayList;
import java.util.List;

public class TasksChain <T extends TasksChain.Task> {
	private boolean pause;

	public abstract class Task {
		public abstract void process() throws Exception;
	}

	private List<T> tasks;

	private int currentTask;

	public TasksChain() {
		tasks = new ArrayList<>();
	}

	public TasksChain addTask(T task) {
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
}
