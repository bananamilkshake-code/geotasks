package me.jtalk.android.geotasks.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.mapsforge.core.model.LatLong;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.jtalk.android.geotasks.util.Logger;

@AllArgsConstructor
public class TaskCoordinates implements Parcelable {
	private static final Logger LOG = new Logger(TaskCoordinates.class);

	@Getter
	@Setter
	private double latitude;

	@Getter
	@Setter
	private double longitude;

	public TaskCoordinates(Location location) {
		this(location.getLatitude(), location.getLongitude());
	}

	public TaskCoordinates(LatLong latLong) {
		this(latLong.latitude, latLong.longitude);
	}

	protected TaskCoordinates(Parcel in) {
		latitude = in.readDouble();
		longitude = in.readDouble();
	}

	public static final Creator<TaskCoordinates> CREATOR = new Creator<TaskCoordinates>() {
		@Override
		public TaskCoordinates createFromParcel(Parcel in) {
			return new TaskCoordinates(in);
		}

		@Override
		public TaskCoordinates[] newArray(int size) {
			return new TaskCoordinates[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(getLatitude());
		dest.writeDouble(getLongitude());
	}

	public double distanceTo(TaskCoordinates taskCoordinates) {
		return toLatLong().distance(taskCoordinates.toLatLong());
	}

	public LatLong toLatLong() {
		return new LatLong(getLatitude(), getLongitude());
	}

	public static TaskCoordinates search(@NonNull Context context, @NonNull String addressText) {
		try {
			Geocoder geocoder = new Geocoder(context);
			if (!geocoder.isPresent()) {
				return null;
			}

			List<Address> searched = geocoder.getFromLocationName(addressText, 1);
			if (searched.isEmpty()) {
				return null;
			} else {
				Address address = searched.get(0);
				return new TaskCoordinates(address.getLatitude(), address.getLongitude());
			}
		} catch (IOException exception) {
			LOG.warn("Error on address search");
			return null;
		}
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}

		if (other instanceof TaskCoordinates) {
			TaskCoordinates otherCoordinates = (TaskCoordinates) other;
			return (latitude == otherCoordinates.latitude) && (longitude == otherCoordinates.longitude);
		} else {
			return false;
		}
	}
}
