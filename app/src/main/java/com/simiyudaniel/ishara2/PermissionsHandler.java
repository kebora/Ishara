package com.simiyudaniel.ishara2;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsHandler {
    private static final int REQUEST_PERMISSIONS = 1;
    private final Activity activity;

    public PermissionsHandler(Activity activity) {
        this.activity = activity;
    }

    public boolean checkAndRequestPermissions() {
        String[] permissions = {
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        };

        boolean allPermissionsGranted = true;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSIONS);
        }

        return allPermissionsGranted;
    }

    public void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                Toast.makeText(activity, "All permissions must be granted to use Ishara", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        }
    }
}
