package com.little_wizard.myapplication;

import android.content.Context;
import android.content.pm.PackageManager;

public class PermissionHelper {
    public static boolean checkPermission(Context context, String permission) {
        return (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }
}
