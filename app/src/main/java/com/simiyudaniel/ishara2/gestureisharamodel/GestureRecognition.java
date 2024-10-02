package com.simiyudaniel.ishara2.gestureisharamodel;

import android.content.Context;
import android.graphics.Bitmap;

import com.simiyudaniel.ishara2.R;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class GestureRecognition {

    private Interpreter tflite;
    private static final int INPUT_SIZE = 64;
    private static final int NUM_CLASSES = 10;

    public GestureRecognition(Context context) {
        try {
            tflite = new Interpreter(loadModelFile(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(context.getResources().openRawResourceFd(R.raw.ishara).getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = context.getResources().openRawResourceFd(R.raw.ishara).getStartOffset();
        long declaredLength = context.getResources().openRawResourceFd(R.raw.ishara).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // This method recognizes gestures from the provided Bitmap
    public float[] recognizeGesture(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(resizedBitmap);

        float[][] output = new float[1][NUM_CLASSES];
        tflite.run(byteBuffer, output);

        return output[0]; // Returning the probabilities for each class
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f); // R
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);  // G
                byteBuffer.putFloat((val & 0xFF) / 255.0f);         // B
            }
        }
        return byteBuffer;
    }
}
