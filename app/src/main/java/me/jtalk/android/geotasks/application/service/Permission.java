package me.jtalk.android.geotasks.application.service;

import com.google.common.collect.ImmutableList;

import java.util.List;

import lombok.Getter;
import me.jtalk.android.geotasks.R;

import static android.Manifest.permission.*;

public enum Permission {
    TRACK_LOCATION(R.string.location_service_toast_no_permission_error, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
    MANAGE_CALENDAR(R.string.calendar_access_toast_no_permission_error, READ_CALENDAR, WRITE_CALENDAR),
    PICK_LOCATION(R.string.make_task_toast_event_creation_no_permission, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, INTERNET),;

    @Getter
    private final int errorMessageId;
    private final List<String> permissions;

    Permission(int errorMessageId, String... permissions) {
        this.errorMessageId = errorMessageId;
        this.permissions = ImmutableList.copyOf(permissions);
    }

    public String[] getPermissions() {
        return permissions.toArray(new String[permissions.size()]);
    }
}
