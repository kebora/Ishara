package com.simiyudaniel.ishara2.timer;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

public class TimerFunction {

    private TextView timerText;
    static int countdownTime = 5;
    private TimerCallback callback;

    public TimerFunction(TextView timerText, int countdownTime, TimerCallback callback) {
        this.timerText = timerText;
        this.countdownTime = countdownTime;
        this.callback = callback;
    }

    public void startCountdown() {
        new CountDownTimer(countdownTime * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerText.setText(String.valueOf(countdownTime - secondsRemaining));
            }

            @Override
            public void onFinish() {
                timerText.setVisibility(View.GONE);
                if (callback != null) {
                    callback.onTimerFinished();
                }
            }
        }.start();
    }

    // Define an interface to handle the callback after the timer finishes
    public interface TimerCallback {
        void onTimerFinished();
    }
}
