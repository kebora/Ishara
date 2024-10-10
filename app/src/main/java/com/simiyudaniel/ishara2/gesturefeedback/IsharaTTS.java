package com.simiyudaniel.ishara2.gesturefeedback;

import android.app.Activity;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class IsharaTTS implements TextToSpeech.OnInitListener {
    /***
     * todo: needs internet connection
     ** can make user use internet if they need a reply for every gesture.
     *  [Having the files appended will increase size of app significantly]
     **/
    public void convertTextToSpeech(String text, Activity a){
        TextToSpeech textToSpeech = new TextToSpeech(a,this);
        textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
        textToSpeech.setLanguage(Locale.ENGLISH);
    }

    @Override
    public void onInit(int status) {

    }
}
