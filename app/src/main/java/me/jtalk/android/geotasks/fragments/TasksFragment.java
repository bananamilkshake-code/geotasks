package me.jtalk.android.geotasks.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.jtalk.android.geotasks.R;

public class TasksFragment extends ListFragment {

	public TasksFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);
		View inflated = inflater.inflate(R.layout.fragment_tasks, container, false);
		// Add contents
		return inflated;
	}
}
