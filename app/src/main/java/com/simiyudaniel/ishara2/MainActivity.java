package com.simiyudaniel.ishara2;

import org.opencv.android.Camera2Renderer;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity implements CvCameraViewListener2,View.OnClickListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;

    private int cameraIndex = CameraBridgeViewBase.CAMERA_ID_ANY;

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        //OpenCV
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = findViewById(R.id.activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        // Change the active camera
        FloatingActionButton flipCameraBtn = (FloatingActionButton)findViewById(R.id.fab_flip_camera);
        flipCameraBtn.setOnClickListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.enableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat dest = new Mat();
        // rotate the mat so that the image is upright
        Core.rotate(inputFrame.rgba(),dest,Core.ROTATE_90_CLOCKWISE);
        return dest;
    }
    //check for the availability of the front camera
    private static boolean hasFrontCamera(Context context) {
        if (context == null) return false;
        // Check for at least one camera
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras == 0) {
            return false;
        }

        // Loop through all cameras to see if any is front-facing
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return true;
            }
        }
        return false;
    }
    // switch camera from front to back and vice versa
    void switchCamera() {
        if (cameraIndex == CameraBridgeViewBase.CAMERA_ID_ANY) {
            // First time opening camera, check for front camera availability
            if (hasFrontCamera(this)){
                cameraIndex =CameraBridgeViewBase.CAMERA_ID_FRONT;
            }
        } else {
            // Switch between back and front camera
            cameraIndex = (cameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT) ? CameraBridgeViewBase.CAMERA_ID_BACK : CameraBridgeViewBase.CAMERA_ID_FRONT;
        }

        // Disable and re-enable camera view to apply changes
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(cameraIndex);
        mOpenCvCameraView.enableView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_flip_camera){
            switchCamera();
        }
    }
}