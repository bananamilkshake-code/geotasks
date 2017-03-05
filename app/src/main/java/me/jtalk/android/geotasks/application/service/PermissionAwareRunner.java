package me.jtalk.android.geotasks.application.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import lombok.RequiredArgsConstructor;
import me.jtalk.android.geotasks.util.Consumer;
import me.jtalk.android.geotasks.util.Logger;

import static me.jtalk.android.geotasks.util.ArrayHelper.arrayOf;
import static me.jtalk.android.geotasks.util.Assert.verifyArgument;
import static me.jtalk.android.geotasks.util.Assert.verifyState;

@RequiredArgsConstructor
public class PermissionAwareRunner {

    private static final Logger LOG = new Logger(PermissionAwareRunner.class);

    private final Activity parent;
    private final Consumer<Permission> errorHandler;
    private final EnumMap<Permission, Runnable> pendingActions = new EnumMap<>(Permission.class);

    public void withPermissionsAsync(Permission permission, Runnable action) {
        List<String> lackingPermissions = validatePermissions(permission, this::loadContextPermission);
        if (lackingPermissions.isEmpty()) {
            new Handler().post(action);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestDynamicPermissions(permission, lackingPermissions, action);
        } else {
            reportNoPermissions(permission, lackingPermissions);
        }
    }

    public void onPermissionUpdate(String[] permissions, int[] states, int requestId) {
        Permission permission = Permission.values()[requestId];
        Runnable action = pendingActions.remove(permission);
        verifyArgument(action != null, "No action found for a permission permission {0} in permission callback", permission.name());
        List<String> lacking = validatePermissions(permission, this::loadContextPermission);
        if (!lacking.isEmpty()) {
            reportNoPermissions(permission, lacking);
        } else {
            action.run();
        }
    }

    private List<String> validatePermissions(Permission permissions, Function<String, Integer> permissionRequester) {
        ArrayList<String> permissionsLack = new ArrayList<>();
        for (String permission : permissions.getPermissions()) {
            int result = permissionRequester.apply(permission);
            if (result != PermissionChecker.PERMISSION_GRANTED) {
                permissionsLack.add(permission);
            }
        }
        return permissionsLack;
    }

    private int loadContextPermission(String permission) {
        return ContextCompat.checkSelfPermission(parent.getApplicationContext(), permission);
    }

    private void reportNoPermissions(Permission permission, List<String> lacking) {
        LOG.error("Unable to perform action {0} - no permissions granted: {1}", permission.name(), lacking);
        errorHandler.consume(permission);
    }

    @TargetApi(23)
    private void requestDynamicPermissions(Permission permission, List<String> lackingPermissions, Runnable action) {
        verifyState(!pendingActions.containsKey(permission), "Permission requested while another request in progress for {0}", permission.name());
        try {
            pendingActions.put(permission, action);
            parent.requestPermissions(arrayOf(lackingPermissions, String[]::new), permission.ordinal());
        } catch (Exception e) {
            pendingActions.remove(permission);
            throw e;
        }
    }
}
