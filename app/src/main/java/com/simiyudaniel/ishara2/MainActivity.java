package com.simiyudaniel.ishara2;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.simiyudaniel.ishara2.gestureisharamodel.GestureRecognition;
import com.simiyudaniel.ishara2.timer.TimerFunction;
import com.simiyudaniel.ishara2.utils.SoundPlayer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends Activity {

    private static final String TAG = "Camera2Video";
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String videoFilePath;

    private ImageButton recordButton, timer_img_btn;
    private TextView timerText;

    // gesture recognition
    private GestureRecognition gestureRecognition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.timer_text);
        timer_img_btn = findViewById(R.id.timer_img_btn);

        // Initialize GestureRecognition
        gestureRecognition = new GestureRecognition(this);

        timer_img_btn.setOnClickListener(v -> {
            timerText.setVisibility(View.VISIBLE);
            TimerFunction timerFunction = new TimerFunction(timerText, 10, () -> startRecording());

            timerFunction.startCountdown();
        });

        textureView = findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(textureListener);

        recordButton = findViewById(R.id.start_record_img_btn);
        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                SoundPlayer soundPlayer = new SoundPlayer(this, R.raw.alert);
                soundPlayer.playSound();
                stopRecording();
            } else {
                SoundPlayer soundPlayer = new SoundPlayer(this, R.raw.alert);
                soundPlayer.playSound();
                startRecording();
            }
        });
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // Get the latest frame from the camera
            Bitmap bitmap = textureView.getBitmap();

            // Run gesture recognition on the current frame
            float[] gestureProbabilities = gestureRecognition.recognizeGesture(bitmap);

            // Handle the recognized gesture (Example: Toggle recording)
            int recognizedGesture = getMaxIndex(gestureProbabilities);

            switch (recognizedGesture) {
                case 0:
                    // Handle gesture 0 (e.g., start recording)
                    if (!isRecording) {
                        startRecording();
                    }
                    break;
                case 1:
                    // Handle gesture 1 (e.g., stop recording)
                    if (isRecording) {
                        stopRecording();
                    }
                    break;
                // Add more cases based on your gestures
            }
        }
    };


    private void startRecording() {
        if (cameraDevice == null) {
            return;
        }
        try {
            setUpMediaRecorder();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface previewSurface = new Surface(texture);
            Surface recordSurface = mediaRecorder.getSurface();
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(recordSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            cameraCaptureSessions = cameraCaptureSession;
                            updatePreview();
                            runOnUiThread(() -> {
                                mediaRecorder.start();
                                isRecording = true;
                                recordButton.setImageResource(R.drawable.pause_record_icon);
                            });
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            isRecording = false;
            recordButton.setImageResource(R.drawable.start_record_icon);
            saveVideoToGallery();
            openCamera(); // Reopen the camera to reinitialize the preview after stopping the recording
        }
    }

    private void setUpMediaRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

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

        if (videoUri != null) {
            videoFilePath = videoUri.toString();
            mediaRecorder.setOutputFile(getContentResolver().openFileDescriptor(videoUri, "w").getFileDescriptor());
        }

        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(640, 480);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.prepare();
    }

    private void saveVideoToGallery() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.IS_PENDING, 0);
        Uri videoUri = Uri.parse(videoFilePath);
        getContentResolver().update(videoUri, values, null, null);
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    //get gesture with high probability
    private int getMaxIndex(float[] probabilities) {
        int maxIndex = 0;
        float maxValue = probabilities[0];

        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxValue) {
                maxValue = probabilities[i];
                maxIndex = i;
            }
        }

        return maxIndex;
    }

}
