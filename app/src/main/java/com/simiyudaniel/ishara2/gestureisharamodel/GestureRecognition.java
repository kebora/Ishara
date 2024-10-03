package com.simiyudaniel.ishara2.gestureisharamodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer.GestureRecognizerOptions;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult;

public class GestureRecognition {
    private static final String TAG = "GestureRecognition";
    private GestureRecognizer gestureRecognizer;

    public GestureRecognition(Context context) {
        try {
            // Initialize BaseOptions with the relative asset path
            String modelPath = "ishara.task";
            BaseOptions baseOptions = BaseOptions.builder()
                    .setModelAssetPath(modelPath)
                    .build();

            // GestureRecognizerOptions for IMAGE mode
            GestureRecognizerOptions options = GestureRecognizerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setNumHands(1)
                    .setRunningMode(RunningMode.IMAGE)
                    .build();

            // Create the GestureRecognizer instance
            gestureRecognizer = GestureRecognizer.createFromOptions(context, options);

            Log.d(TAG, "GestureRecognizer initialized successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize GestureRecognizer: ", e);
        }
    }

    /**
     * Recognize gesture from a Bitmap image.
     *
     * @param bitmap Input image from the camera.
     * @return Recognized gesture with confidence as a String.
     */
    public String recognizeGesture(Bitmap bitmap) {
        if (gestureRecognizer == null) {
            Log.e(TAG, "GestureRecognizer is not initialized.");
            return "Recognizer Not Initialized";
        }

        if (bitmap == null) {
            Log.e(TAG, "Input bitmap is null.");
            return "Invalid Input";
        }

        try {
            // Convert Bitmap to MediaPipe Image
            MPImage mediaPipeImage = new BitmapImageBuilder(bitmap).build();

            // Perform gesture recognition
            GestureRecognizerResult result = gestureRecognizer.recognize(mediaPipeImage);

            // Release the image resources
            mediaPipeImage.close();

            if (result.gestures().isEmpty()) {
                return "No Gesture";
            }

            // maxResults = 1
            Category gesture = result.gestures().get(0).get(0);
            String gestureName = gesture.categoryName();
            float confidence = gesture.score();


            System.out.println("Recognized Gesture: " +gestureName);
            Log.d(TAG, "Recognized Gesture: " + gestureName + " with confidence: " + confidence);

            return gestureName + " (" + String.format("%.2f", confidence) + ")";
        } catch (Exception e) {
            Log.e(TAG, "Error during gesture recognition: ", e);
            return "Error";
        }
    }

    /**
     * Closes the GestureRecognizer and releases resources.
     */
    public void close() {
        if (gestureRecognizer != null) {
            gestureRecognizer.close();
            gestureRecognizer = null;
            Log.d(TAG, "GestureRecognizer closed.");
        }
    }
}
