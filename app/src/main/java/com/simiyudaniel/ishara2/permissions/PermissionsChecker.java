package com.simiyudaniel.ishara2.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionsChecker{
    // Confirms all permissions are there
    public boolean hasAllPermissions(Activity a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(a, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(a, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(a, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(a, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(a, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(a, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
    //
}
