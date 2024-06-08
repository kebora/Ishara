package com.simiyudaniel.ishara2;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.content.pm.PackageManager;
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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.OutputStream;


public class MainActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener, SurfaceHolder.Callback, PermissionsHandler.PermissionsHandlerCallback {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private PermissionsHandler permissionsHandler;
    private String outputFileName;
    private ImageButton startRecordBtn;

    private static final int REQUEST_PERMISSIONS = 1;

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
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);



        permissionsHandler = new PermissionsHandler(this);

        if (!permissionsHandler.checkAndRequestPermissions()) {
            return; // Permissions not granted
        }
        requestPermissions();

        initializeCameraView();
    }

    private void initializeCameraView() {
        mOpenCvCameraView = findViewById(R.id.activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        startRecordBtn = findViewById(R.id.start_record_btn);
        startRecordBtn.setOnClickListener(this);

        SurfaceHolder holder = mOpenCvCameraView.getHolder();
        holder.addCallback(this);
    }
private void requestPermissions() {
    String[] permissions = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    if (!hasPermissions(this, permissions)) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
    }
}

    private boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsHandler.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCameraView();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
        if (isRecording) stopRecord();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOpenCvCameraView != null) mOpenCvCameraView.enableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
        if (isRecording) stopRecord();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Toast.makeText(this, "Camera view starts...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_record_btn) {
            if (isRecording) {
                stopRecord();
                startRecordBtn.setImageResource(R.drawable.start_record_icon);
            } else {
                try {
                    startRecord();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                startRecordBtn.setImageResource(R.drawable.pause_record_icon);
            }
        }
    }

    private void startRecord() throws FileNotFoundException {
        if (prepareMediaRecorder()) {
            try {
                mediaRecorder.start();
                isRecording = true;
                Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Recording started...");
            } catch (IllegalStateException e) {
                Log.e(TAG, "IllegalStateException starting MediaRecorder: " + e.getMessage());
                Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
                releaseMediaRecorder();
            }
        } else {
            Toast.makeText(this, "Failed to prepare MediaRecorder", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to prepare MediaRecorder");
        }
    }

    private void stopRecord() {
        try {
            mediaRecorder.stop();
            Toast.makeText(this, "Recording stopped...", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Recording stopped...");
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException stopping MediaRecorder: " + e.getMessage());
            // Cleanup partially written file
            File file = new File(outputFileName);
            if (file.exists() && file.delete()) {
                Log.i(TAG, "Deleted incomplete file: " + outputFileName);
            }
        } finally {
            releaseMediaRecorder();
            isRecording = false;
        }
    }

    private boolean prepareMediaRecorder() throws FileNotFoundException {
        mOpenCvCameraView.disableView();

        mediaRecorder = new MediaRecorder();

        mOpenCvCameraView.enableView();

        // Configure MediaRecorder
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoSize(640, 480);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoEncodingBitRate(10000000);

        // Create file using MediaStore
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "video_" + timeStamp + ".mp4";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Ishara");
        values.put(MediaStore.Video.Media.TITLE, fileName);
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri videoUri = getContentResolver().insert(collection, values);

        if (videoUri == null) {
            Log.e(TAG, "Failed to create new MediaStore record.");
            return false;
        }

        outputFileName = videoUri.toString();
        Log.i(TAG, "Output file: " + outputFileName);

        mediaRecorder.setOutputFile(getContentResolver().openFileDescriptor(videoUri, "w").getFileDescriptor());
        mediaRecorder.setPreviewDisplay(mOpenCvCameraView.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            mediaRecorder.release();
            mediaRecorder = null;
            return false;
        }

        return true;
    }


    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    public void onPermissionsGranted() {
        initializeCameraView();
    }
}
