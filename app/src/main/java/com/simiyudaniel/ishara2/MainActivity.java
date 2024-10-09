package com.simiyudaniel.ishara2;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.simiyudaniel.ishara2.gesturefeedback.GestureFeedback;
import com.simiyudaniel.ishara2.gestureisharamodel.GestureRecognition;
import com.simiyudaniel.ishara2.permissions.PermissionsChecker;
import com.simiyudaniel.ishara2.timer.TimerFunction;
import com.simiyudaniel.ishara2.utils.SoundPlayer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSIONS_CODE = 1001;

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private MediaRecorder mediaRecorder;
    private boolean isRecording,isPaused = false;
    private String videoFilePath;

    private ImageButton recordButton, timerImgBtn;
    private TextView timerText, gestureTextView;

    // Gesture recognition
    private GestureRecognition gestureRecognition;

    // ExecutorService for background tasks
    private ExecutorService executorService;

    //PermissionsChecker
    PermissionsChecker permissionsChecker = new PermissionsChecker();

    //Feedback on gesture detected
    GestureFeedback gestureFeedback;

    //
    private Chronometer recordingTimer;
    private long pauseOffset = 0;

    // for camera flipping
    private boolean isUsingFrontCamera = false;
    ImageButton flipCameraBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        // Initialize UI components
        textureView = findViewById(R.id.texture_view);
        recordButton = findViewById(R.id.start_record_img_btn);
        timerImgBtn = findViewById(R.id.timer_img_btn);
        timerText = findViewById(R.id.timer_text);
        gestureTextView = findViewById(R.id.gesture_text_view);

        //flip camera button
        flipCameraBtn = findViewById(R.id.switch_camera_img_btn);
        flipCameraBtn.setOnClickListener(v -> {
            isUsingFrontCamera = !isUsingFrontCamera;
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            openCamera(isUsingFrontCamera);
        });

        // Initialize GestureRecognition
        gestureRecognition = new GestureRecognition(this);
        //
        gestureFeedback = new GestureFeedback(this,this);

        // Set up TextureView listener
        textureView.setSurfaceTextureListener(textureListener);

        // recording timer
        recordingTimer = findViewById(R.id.recording_timer);

        // Set up Record Button click listener
        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        // Set up Timer Image Button click listener
        timerImgBtn.setOnClickListener(v -> {
            timerText.setVisibility(TextView.VISIBLE);
            TimerFunction timerFunction = new TimerFunction(timerText, 10, () -> {
                if (!isRecording) {
                    startRecording();
                }
            });
            timerFunction.startCountdown();
        });

        // Check and request permissions
        if (!permissionsChecker.hasAllPermissions(this)) {
            requestNecessaryPermissions();
        } else {
            // Proceed with camera setup
            openCamera(isUsingFrontCamera);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown executor safely
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * Requests all necessary permissions at runtime.
     */
    private void requestNecessaryPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) ||
                    (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) && shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) && shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE))) {

                // Prompt for permission
                new AlertDialog.Builder(this)
                        .setTitle("Permissions Required")
                        .setMessage("This app requires Camera and Audio permissions to function correctly.")
                        .setPositiveButton("Grant", (dialog, which) -> {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    getRequiredPermissions(),
                                    REQUEST_PERMISSIONS_CODE);
                        })
                        .setNegativeButton("Deny", (dialog, which) -> {
                            Toast.makeText(MainActivity.this, "Permissions not granted. App cannot function.", Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .create()
                        .show();
            } else {
                // Request the permissions
                ActivityCompat.requestPermissions(this,
                        getRequiredPermissions(),
                        REQUEST_PERMISSIONS_CODE);
            }
        }
    }

    /**
     * Returns an array of required permissions based on SDK version.
     *
     * @return Array of permission strings.
     */
    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
        } else {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }
    }

    /**
     * Callback for the result from requesting permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.length > 0) {
                boolean allGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    // Proceed with camera setup
                    openCamera(isUsingFrontCamera);
                } else {
                    // todo: Don't exit app::Make it more informative.
                    // Permissions denied
                    Toast.makeText(this, "Permissions not granted. App cannot function.", Toast.LENGTH_LONG).show();
                    finish(); // Close app
                }
            } else {
                // Permissions denied
                Toast.makeText(this, "Permissions not granted. App cannot function.", Toast.LENGTH_LONG).show();
                finish(); // Close the app
            }
        }
    }

    /**
     * Opens the camera by accessing the CameraManager and requesting camera access.
     */

    private void openCamera(boolean useFrontCamera) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // 0 for the back camera: 1 for the front camera
            String cameraId = useFrontCamera ? manager.getCameraIdList()[1] : manager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException: " + e.getMessage());
        }
    }


    /**
     * Callback for camera state changes.
     */
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
            Log.d(TAG, "Camera opened.");
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
            Log.d(TAG, "Camera disconnected.");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
            Log.e(TAG, "Camera error: " + error);
        }
    };

    /**
     * Creates the camera preview by setting up a CaptureRequest and starting the camera session.
     */
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) {
                Log.e(TAG, "SurfaceTexture is null.");
                return;
            }
            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null) {
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                    Log.d(TAG, "Camera capture session configured.");
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration Change", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Camera capture session configuration failed.");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException in createCameraPreview: " + e.getMessage());
        }
    }

    /**
     * Peak Mode
     *  Temporarily switch views for a set number of seconds
     */

    private Handler handler = new Handler();
    private Runnable toggleCameraRunnable;
    private boolean isPeakModeActive = false;
    // 1 second toggle interval
    private int toggleInterval = 1000;

    public void triggerPeakMode(int seconds) {
        if (!isRecording) {
            if (isPeakModeActive) {
                return;
            }

            isPeakModeActive = true;
            int totalTimeInMillis = seconds * 1000;
            final int numberOfToggles = totalTimeInMillis / toggleInterval;
            final boolean initialCameraState = isUsingFrontCamera;

            toggleCameraRunnable = new Runnable() {
                int toggleCount = 0;

                @Override
                public void run() {
                    // Toggle camera
                    isUsingFrontCamera = !isUsingFrontCamera;
                    if (cameraDevice != null) {
                        cameraDevice.close();
                        cameraDevice = null;
                    }
                    // Open the new camera
                    openCamera(isUsingFrontCamera);

                    toggleCount++;
                    if (toggleCount < numberOfToggles) {
                        // Schedule the next toggle
                        handler.postDelayed(this, toggleInterval);
                    } else {
                        // Stop peak mode after toggling is complete
                        isPeakModeActive = false;
                        isUsingFrontCamera = initialCameraState;
                        if (cameraDevice != null) {
                            cameraDevice.close();
                            cameraDevice = null;
                        }
                        // go back to original camera
                        openCamera(isUsingFrontCamera);
                    }
                }
            };

            // Start the camera toggling
            handler.post(toggleCameraRunnable);
        }
    }

    /**
     * Opens the camera for preview without affecting MediaRecorder session.
     */
    private void openPreviewOnly(String cameraId) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException: " + e.getMessage());
        }
    }




    /**
     * Updates the camera preview by setting the repeating request.
     */
    private void updatePreview() {
        if (cameraDevice == null) {
            Log.e(TAG, "CameraDevice is null in updatePreview.");
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
            Log.d(TAG, "Camera preview updated.");
        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException in updatePreview: " + e.getMessage());
        }
    }

    /**
     * Starts video recording by setting up MediaRecorder and configuring the camera session.
     */
    public void startRecording() {
        if (cameraDevice == null) {
            Log.e(TAG, "Cannot start recording: CameraDevice is null.");
            return;
        }
        try {
            setUpMediaRecorder();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) {
                Log.e(TAG, "SurfaceTexture is null in startRecording.");
                return;
            }
            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface previewSurface = new Surface(texture);
            Surface recordSurface = mediaRecorder.getSurface();

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(recordSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                    runOnUiThread(() -> {
                        try {
                            mediaRecorder.start();
                            isRecording = true;

                            // Start and display the recording timer
                            recordingTimer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                            recordingTimer.start();
                            recordingTimer.setVisibility(View.VISIBLE);

                            // Update button and notify user
                            recordButton.setImageResource(R.drawable.pause_record_icon);
                            Toast.makeText(MainActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Recording started.");
                        } catch (IllegalStateException e) {
                            Log.e(TAG, "Error starting MediaRecorder: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration Change", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Camera capture session configuration failed during recording.");
                }
            }, null);
        } catch (CameraAccessException | IOException e) {
            Log.e(TAG, "Exception in startRecording: " + e.getMessage());
        }
    }


    /**
     * Pause Recording
     */
    public void pauseRecording() {
        if (isRecording) {
            mediaRecorder.pause();
            isPaused = true;
            isRecording = false;

            // Pause the recording timer and save the offset
            recordingTimer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - recordingTimer.getBase();
            recordButton.setImageResource(R.drawable.stop_record_icon);
            Log.d(TAG, "Recording paused.");
        }
    }

    /**
     * Resume Recording
     */
    public void resumeRecording() {
        if (isRecording && isPaused) {
            mediaRecorder.resume();
            isPaused = false;
            isRecording = true;

            // Resume the timer from where it left off
            recordingTimer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            recordingTimer.start();
            recordButton.setImageResource(R.drawable.pause_record_icon);
            Log.d(TAG, "Recording resumed.");
        }
    }

    /**
     * Stops video recording and saves the video to the gallery.
     */
    public void stopRecording() {
        if (!isRecording) {
            Log.e(TAG, "Cannot stop recording: Not currently recording.");
            return;
        }
        try {
            mediaRecorder.stop();
            mediaRecorder.reset();
            isRecording = false;
            // Stop and reset the timer
            recordingTimer.stop();
            recordingTimer.setVisibility(View.GONE);
            pauseOffset = 0; // Reset the pause offset
            recordButton.setImageResource(R.drawable.start_record_icon);
            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Recording stopped.");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.IS_PENDING, 0);
                getContentResolver().update(Uri.parse(videoFilePath), values, null, null);
            }
            // Reopen the camera to restart the preview
            openCamera(isUsingFrontCamera);

        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException while stopping MediaRecorder: " + e.getMessage());
            Toast.makeText(this, "Failed to stop recording properly.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets up MediaRecorder with the desired configurations.
     */
    private void setUpMediaRecorder() throws IOException, CameraAccessException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // Generate a unique file name based on timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "ISHARA_" + timeStamp + ".mp4";

        ContentValues values = new ContentValues();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Ishara");
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        } else {
            values.put(MediaStore.Video.Media.DATA, getExternalFilesDir(null) + "/ISHARA_" + timeStamp + ".mp4");
        }

        values.put(MediaStore.Video.Media.TITLE, fileName);
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        Uri videoUri = getContentResolver().insert(collection, values);
        if (videoUri != null) {
            videoFilePath = videoUri.toString();
            mediaRecorder.setOutputFile(getContentResolver().openFileDescriptor(videoUri, "w").getFileDescriptor());
        } else {
            throw new IOException("Failed to create MediaStore entry.");
        }

        // Set video configurations
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
//        mediaRecorder.setVideoSize(1920, 1080);
        mediaRecorder.setVideoSize(1280, 720);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        // Set orientation hint based on device rotation
        // Calculate orientation hint based on camera sensor orientation and device rotation
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int sensorOrientation = getCameraSensorOrientation(); // Get sensor orientation
        int orientationHint = (sensorOrientation + getDeviceRotationDegrees(rotation-90+360)) % 360;

        mediaRecorder.setOrientationHint(orientationHint);
        mediaRecorder.prepare();
        Log.d(TAG, "MediaRecorder configured.");
    }
    // Helper function to get camera sensor orientation
    private int getCameraSensorOrientation() throws CameraAccessException {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
        return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
    }

    // Helper function to map Surface rotation to degrees
    private int getDeviceRotationDegrees(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                return 90;
            case Surface.ROTATION_90:
                return 0;
            case Surface.ROTATION_180:
                return 270;
            case Surface.ROTATION_270:
                return 180;
            default:
                return 0;
        }
    }


    /**
     * Lifecycle method to release resources when the activity is paused.
     */
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (isRecording) {
//            stopRecording();
//        }
//        if (cameraDevice != null) {
//            cameraDevice.close();
//            cameraDevice = null;
//            Log.d(TAG, "CameraDevice closed in onPause.");
//        }
//        if (mediaRecorder != null) {
//            mediaRecorder.release();
//            mediaRecorder = null;
//            Log.d(TAG, "MediaRecorder released in onPause.");
//        }
//        if (gestureRecognition != null) {
//            gestureRecognition.close();
//            Log.d(TAG, "GestureRecognition closed in onPause.");
//        }
//        if (executorService != null) {
//            executorService.shutdown();
//            Log.d(TAG, "ExecutorService shut down in onPause.");
//        }
//    }
//
//    /**
//     * Lifecycle method to re-initialize resources when the activity resumes.
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (textureView.isAvailable() && !permissionsChecker.hasAllPermissions(this)) {
//            openCamera();
//        } else {
//            textureView.setSurfaceTextureListener(textureListener);
//        }
//    }

    private boolean shouldResumeRecording = false;

    @Override
    protected void onPause() {
        super.onPause();

        if (isRecording) {
            stopRecording();
            shouldResumeRecording = true; // Indicate that recording should resume on return
        }

        // Release resources only if they're currently in use
        closeCameraSafely();
        releaseMediaRecorderSafely();
        releaseGestureRecognitionSafely();
        shutdownExecutorService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (permissionsChecker.hasAllPermissions(this)) {
            // If the texture is available, set up the camera and start preview
            if (textureView.isAvailable()) {
                openCamera(isUsingFrontCamera);
                if (shouldResumeRecording) {
                    startRecording(); // Restart recording if it was interrupted by the pause
                    shouldResumeRecording = false;
                }
            } else {
                // Set listener to reopen camera when texture is ready
                textureView.setSurfaceTextureListener(textureListener);
            }
        } else {
            // Handle permissions if they are not granted
            requestNecessaryPermissions();
        }
    }

    // Methods to safely release resources only if they are non-null
    private void closeCameraSafely() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
            Log.d(TAG, "CameraDevice safely closed.");
        }
    }

    private void releaseMediaRecorderSafely() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
            Log.d(TAG, "MediaRecorder safely released.");
        }
    }

    private void releaseGestureRecognitionSafely() {
        if (gestureRecognition != null) {
            gestureRecognition.close();
            gestureRecognition = null;
            Log.d(TAG, "GestureRecognition safely closed.");
        }
    }

    private void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            executorService = null;
            Log.d(TAG, "ExecutorService shut down.");
        }
    }
    /**
     * Gesture TextureView listener.
     * Handles the SurfaceTexture updates for gesture recognition.
     */
    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            // Already handled in onCreate after permissions
            if (permissionsChecker.hasAllPermissions(MainActivity.this)) {
                openCamera(isUsingFrontCamera);
            } else {
                requestNecessaryPermissions();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            // Handle size changes
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }
        private long lastRecognitionTime = 0;
        private static final long RECOGNITION_INTERVAL_MS = 500;

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            // Get the latest frame from the camera as a Bitmap
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRecognitionTime < RECOGNITION_INTERVAL_MS) {
                return;
            }
            lastRecognitionTime = currentTime;
//            Bitmap bitmap = textureView.getBitmap();
            Bitmap bitmap = textureView.getBitmap(320, 240);

            if (bitmap != null) {
                // Run gesture recognition on a background thread
                executorService.execute(() -> {
                    String recognizedGesture = gestureRecognition.recognizeGesture(bitmap);

                    // Update UI based on recognized gesture
                    runOnUiThread(() -> {
                        gestureTextView.setText("Gesture: " + recognizedGesture);
                        gestureFeedback.handleGesture(recognizedGesture);
                    });
                });
            }
        }
    };
}
