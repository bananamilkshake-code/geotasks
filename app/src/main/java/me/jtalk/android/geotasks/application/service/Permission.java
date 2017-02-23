package me.jtalk.android.geotasks.application.service;

import android.Manifest;

import com.google.common.collect.ImmutableList;

import java.util.List;

import lombok.Getter;
import me.jtalk.android.geotasks.R;

public enum Permission {
    TRACK_LOCATION(R.string.location_service_toast_no_permission_error, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
    MANAGE_CALENDAR(R.string.calendar_access_toast_no_permission_error, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
    ;

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
