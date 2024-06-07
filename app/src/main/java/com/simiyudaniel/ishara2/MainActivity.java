package com.simiyudaniel.ishara2;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends CameraActivity implements CvCameraViewListener2, View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        ImageButton startRecordBtn = findViewById(R.id.start_record_btn);
        startRecordBtn.setOnClickListener(this);

        SurfaceHolder holder = mOpenCvCameraView.getHolder();
        holder.addCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if (isRecording) {
            stopRecord();
        }
    }

    @Override
    public void onResume() {
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
        if (isRecording) {
            stopRecord();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Toast.makeText(this,"Camera view starts...",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_record_btn) {
            if (isRecording) {
                stopRecord();
            } else {
                // todo:change the background asset
                startRecord();
            }
        }
    }

    private void startRecord() {
        if (prepareMediaRecorder()) {
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Recording started...");
        } else {
            Toast.makeText(this, "Failed to prepare MediaRecorder", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to prepare MediaRecorder");
        }
    }

    private void stopRecord() {
        try {
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException stopping MediaRecorder: " + e.getMessage());
            // Cleanup partially written file
            mediaRecorder.reset();
        }
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        Toast.makeText(this, "Recording stopped...", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Recording stopped...");
    }

    private boolean prepareMediaRecorder() {
        mOpenCvCameraView.disableView();

        mediaRecorder = new MediaRecorder();

        mOpenCvCameraView.enableView();

        // Assuming mOpenCvCameraView is properly initialized and started
        Surface surface = mOpenCvCameraView.getHolder().getSurface();

        // Configure MediaRecorder
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoSize(640, 480);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoEncodingBitRate(10000000);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String outputFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/video_" + timeStamp + ".mp4";
        mediaRecorder.setOutputFile(outputFileName);

        mediaRecorder.setPreviewDisplay(surface);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            mediaRecorder.release();
            return false;
        }

        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Handle surface creation if needed
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Handle surface changes if needed
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Handle surface destruction if needed
    }
}
