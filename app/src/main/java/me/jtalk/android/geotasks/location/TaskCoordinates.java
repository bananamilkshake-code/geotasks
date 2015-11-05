package me.jtalk.android.geotasks.location;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class TaskCoordinates implements Parcelable {
	@Getter
	@Setter
	private double latitude;

	@Getter
	@Setter
	private double longitude;

	public TaskCoordinates(Location location) {
		this(location.getLatitude(), location.getLongitude());
	}

	public TaskCoordinates(IGeoPoint geoPoint) {
		this(geoPoint.getLatitude(), geoPoint.getLongitude());
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

	public int distanceTo(TaskCoordinates taskCoordinates) {
		return toGeoPoint().distanceTo(taskCoordinates.toGeoPoint());
	}

	public GeoPoint toGeoPoint() {
		return new GeoPoint(getLatitude(), getLongitude());
	}
}
