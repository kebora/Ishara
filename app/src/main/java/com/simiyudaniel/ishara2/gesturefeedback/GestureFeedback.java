package com.simiyudaniel.ishara2.gesturefeedback;

import android.speech.tts.TextToSpeech;
import android.util.Log;

public class GestureFeedback {
    public void handleGesture(String gesture) {
        if (gesture.contains("palm")) {
            Log.d("GestureFeedback", "Palm detected");
        } else if (gesture.contains("like")) {
            Log.d("GestureFeedback", "Like detected");
        } else if (gesture.contains("fist")) {
            Log.d("GestureFeedback", "Fist detected");
        } else if (gesture.contains("peace")) {
            Log.d("GestureFeedback", "Peace detected");
        } else if (gesture.contains("ok")) {
            Log.d("GestureFeedback", "OK detected");
        } else if (gesture.contains("stop")) {
            Log.d("GestureFeedback", "Stop detected");
        } else if (gesture.contains("one")) {
            Log.d("GestureFeedback", "One detected");
        } else {
            Log.d("GestureAction", "Unknown gesture: " + gesture);
        }
    }
    //
}
