package com.simiyudaniel.ishara2.gesturefeedback;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.simiyudaniel.ishara2.GesturesFragment;
import com.simiyudaniel.ishara2.MainActivity;

public class GestureFeedback {

    private final MainActivity mainActivity;
    private final SharedPreferences sharedPreferences;

    // Gesture labels used in shared preferences
    private final String[] labels = {"fist", "ok", "like", "one", "peace", "palm", "stop"};

    public GestureFeedback(Context context, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.sharedPreferences = context.getSharedPreferences("gesturesdb", Context.MODE_PRIVATE);
    }

    // Method to handle gestures
    public void handleGesture(String gesture) {
        String gestureLabel = getGestureLabel(gesture);

        if (gestureLabel != null) {
            // Retrieve the saved functionality for the gesture from shared preferences
            String savedFunctionality = sharedPreferences.getString(gestureLabel, "DISABLE");
            GesturesFragment.GestureFunctionalities functionality = GesturesFragment.GestureFunctionalities.valueOf(savedFunctionality);
            //
            executeFunctionality(functionality);
        } else {
            Log.d("GestureAction", "Unknown gesture: " + gesture);
        }
    }

    // Map detected gesture to a label
    private String getGestureLabel(String gesture) {
        for (String label : labels) {
            if (gesture.contains(label)) {
                return label;
            }
        }
        return null;
    }

    // Execute based on the enum value
    private void executeFunctionality(GesturesFragment.GestureFunctionalities functionality) {
        switch (functionality) {
            case START_RECORDING:
                mainActivity.startRecording();
                break;
            case PAUSE_RECORDING:
                mainActivity.pauseRecording();
                break;
            case STOP_RECORDING:
                mainActivity.stopRecording();
                break;
            case RESUME_RECORDING:
                mainActivity.resumeRecording();
                break;
            case PEEK_MODE:
                mainActivity.triggerPeakMode(10);
                break;
            case START_TIMER:
                mainActivity.triggerTimerBtn();
                break;
            case DISABLE:
            default:
                break;
        }
    }

}
