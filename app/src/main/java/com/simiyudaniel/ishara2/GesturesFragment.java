package com.simiyudaniel.ishara2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

public class GesturesFragment extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gestures, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_gestures, null);

        NumberPicker numberPickerTimer = view.findViewById(R.id.numberPickerTimer);
        NumberPicker numberPickerVideoDuration = view.findViewById(R.id.numberPickerVideoDuration);


        builder.setView(view)
                .setTitle("Gesture Settings")
                .setPositiveButton("OK", (dialog, id) -> {
                    int timerDuration = numberPickerTimer.getValue();
                    int videoDuration = numberPickerVideoDuration.getValue();

                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });
        return builder.create();
    }
}