package com.simiyudaniel.ishara2.gesturefeedback;

import android.content.Context;
import android.util.Log;

import com.simiyudaniel.ishara2.MainActivity;


public class GestureFeedback {
    private Context context;
    private MainActivity mainActivity;
    //
    public GestureFeedback(Context context,MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
    }
    //
    public void handleGesture(String gesture) {
        if (gesture.contains("palm"))
        {
//            mainActivity.pauseRecording();
            Log.d("GestureFeedback", "Palm detected");
            //
        } else if (gesture.contains("like"))
        {
//            mainActivity.resumeRecording();
            Log.d("GestureFeedback", "Like detected");
            //
        } else if (gesture.contains("fist"))
        {
//            mainActivity.startRecording();
            Log.d("GestureFeedback", "Fist detected");
        } else if (gesture.contains("peace"))
        {

            Log.d("GestureFeedback", "Peace detected");
        } else if (gesture.contains("ok"))
        {
//            mainActivity.stopRecording();
            Log.d("GestureFeedback", "OK detected");
        } else if (gesture.contains("stop"))
        {
            Log.d("GestureFeedback", "Stop detected");
        } else if (gesture.contains("one"))
        {
            Log.d("GestureFeedback", "One detected");
        } else
        {
            Log.d("GestureAction", "Unknown gesture: " + gesture);
        }
    }
}
