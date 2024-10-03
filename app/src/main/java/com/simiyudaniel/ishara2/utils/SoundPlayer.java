package com.simiyudaniel.ishara2.utils;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundPlayer {
    private MediaPlayer mediaPlayer;

    public SoundPlayer(Context context, int soundResourceId) {
        mediaPlayer = MediaPlayer.create(context, soundResourceId);
    }

    // Method to play the sound
    public void playSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    // Method to release the MediaPlayer resources
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
