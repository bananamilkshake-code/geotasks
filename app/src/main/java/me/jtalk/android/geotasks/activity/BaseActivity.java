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
package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.support.annotation.NonNull;

import me.jtalk.android.geotasks.application.service.Permission;
import me.jtalk.android.geotasks.application.service.PermissionAwareRunner;
import me.jtalk.android.geotasks.util.Logger;

public abstract class BaseActivity extends Activity {

    protected final Logger log = new Logger(getClass());
    private final PermissionAwareRunner permissionAwareRunner = new PermissionAwareRunner(this, this::onNoPermission);

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] values) {
        permissionAwareRunner.onPermissionUpdate(permissions, values, requestCode);
    }

    protected void withPermissionsAsync(Permission permission, Runnable action) {
        permissionAwareRunner.withPermissionsAsync(permission, action);
    }

    protected void onNoPermission(Permission permission) {
        log.fatal("An unhandled permission failure for {0}", permission);
    }
}
