package com.simiyudaniel.ishara2;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
    private EditText appTimeoutEditText;
    private SharedPreferences sharedPreferences;

    // Default values
    private static final int DEFAULT_TIMER_VALUE = 10;
    private static final int DEFAULT_APP_TIMEOUT_VALUE = 60;

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
        appTimeoutEditText = view.findViewById(R.id.etAppTimeoutValue);

        // Load saved values or set default values
        loadSavedValues();

        builder.setView(view)
                .setTitle("App Settings")
                .setPositiveButton("SAVE", (dialog, which) -> {
                    saveValues();
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void loadSavedValues() {
        // Load saved values or set defaults
        int savedTimerValue = sharedPreferences.getInt("timer_value", DEFAULT_TIMER_VALUE);
        int savedAppTimeoutValue = sharedPreferences.getInt("app_timeout_value", DEFAULT_APP_TIMEOUT_VALUE);

        //
        timerEditText.setText(String.valueOf(savedTimerValue));
        appTimeoutEditText.setText(String.valueOf(savedAppTimeoutValue));
    }

    private void saveValues() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //
        String timerValueText = timerEditText.getText().toString();
        String appTimeoutValueText = appTimeoutEditText.getText().toString();

        int timerValue = TextUtils.isEmpty(timerValueText) ? DEFAULT_TIMER_VALUE : Integer.parseInt(timerValueText);
        int appTimeoutValue = TextUtils.isEmpty(appTimeoutValueText) ? DEFAULT_APP_TIMEOUT_VALUE : Integer.parseInt(appTimeoutValueText);

        // Save the values
        editor.putInt("timer_value", timerValue);
        editor.putInt("app_timeout_value", appTimeoutValue);

        //
        editor.apply();

        Toast.makeText(requireContext(), "Settings saved!", Toast.LENGTH_SHORT).show();
    }
}
