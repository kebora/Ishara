package com.simiyudaniel.ishara2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SettingsFragment extends DialogFragment {

    private EditText timerEditText;
    private SharedPreferences sharedPreferences;

    // Default value::: Timer
    private static final int DEFAULT_TIMER_VALUE = 10;

    public interface SettingsListener {
        void onSettingsSaved(int timerValue);
    }
    //
    private SettingsListener settingsListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            settingsListener = (SettingsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement SettingsListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_settings, null);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("ishara_prefs", Context.MODE_PRIVATE);

        // Initialize EditText fields
        timerEditText = view.findViewById(R.id.etTimerValue);

        // Load saved values
        loadSavedValues();

        builder.setView(view)
                .setTitle("App Settings")
                .setPositiveButton("OK", (dialog, which) -> {
                    saveValues();
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void loadSavedValues() {
        int savedTimerValue = sharedPreferences.getInt("timer_value", DEFAULT_TIMER_VALUE);

        //
        timerEditText.setText(String.valueOf(savedTimerValue));
    }

    private void saveValues() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //
        String timerValueText = timerEditText.getText().toString();

        int timerValue = TextUtils.isEmpty(timerValueText) ? DEFAULT_TIMER_VALUE : Integer.parseInt(timerValueText);

        // Save the values
        editor.putInt("timer_value", timerValue);
        //
        editor.apply();

        // Notify MainActivity about the saved values
        //todo: find a way to allow using new values without restarting.
        // check RXJava
        if (settingsListener != null) {
            settingsListener.onSettingsSaved(timerValue);
        }
        // Notify user to restart app
        Toast.makeText(requireContext(), "Restart App", Toast.LENGTH_LONG).show();
    }
}
