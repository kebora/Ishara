package com.simiyudaniel.ishara2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionsHandler {

    public interface PermissionsHandlerCallback {
        void onPermissionsGranted();
    }

    private static final int REQUEST_PERMISSIONS = 1;
    private final Activity activity;
    private final PermissionsHandlerCallback callback;

    public PermissionsHandler(Activity activity) {
        this.activity = activity;
        this.callback = (PermissionsHandlerCallback) activity;
    }

    public boolean checkAndRequestPermissions() {
        String[] permissions = {
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA,
        };

        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
            return false;
        }

        return true;
    }

    public void handleRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allPermissionsGranted = true;

            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                callback.onPermissionsGranted();
            } else {
                // Handle permission denial appropriately
            }
        }
    }
}
