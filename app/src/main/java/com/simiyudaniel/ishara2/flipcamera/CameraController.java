package com.simiyudaniel.ishara2.flipcamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.view.TextureView;

import androidx.core.app.ActivityCompat;

public class CameraController {
    private Context context;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isUsingFrontCamera = false;
    private CameraDevice.StateCallback stateCallback;

    public CameraController(Context context, TextureView textureView, CameraDevice.StateCallback stateCallback) {
        this.context = context;
        this.textureView = textureView;
        this.stateCallback = stateCallback;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void switchCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        isUsingFrontCamera = !isUsingFrontCamera; // Toggle between front and back camera

        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String id : cameraIdList) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if ((isUsingFrontCamera && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) ||
                        (!isUsingFrontCamera && lensFacing == CameraCharacteristics.LENS_FACING_BACK)) {
                    cameraId = id;
                    break;
                }
            }
            openCamera(); // Reopen the camera with the new cameraId
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
                return;
            }
            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setCameraDevice(CameraDevice cameraDevice) {
        this.cameraDevice = cameraDevice;
    }

    public boolean isUsingFrontCamera() {
        return isUsingFrontCamera;
    }
}
