package com.simiyudaniel.ishara2;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class GesturesFragment extends DialogFragment {

    private ImageSwitcher imageSwitcher;
    private Spinner imageAssociationSpinner;
    private ImageButton nextButton, previousButton;
    private int currentIndex = 0;
    private SharedPreferences sharedPreferences;

    private final int[] imageResources = {R.drawable.fist, R.drawable.ok,
            R.drawable.like, R.drawable.one, R.drawable.peace,
            R.drawable.palm, R.drawable.stop};
    private final String[] labels = {"fist", "ok", "like", "one", "peace", "palm", "stop"};

    // Enum for gesture functionalities
    public enum GestureFunctionalities {
        PEEK_MODE, START_RECORDING, RESUME_RECORDING, PAUSE_RECORDING,
        STOP_RECORDING, DISABLE
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("gesturesdb", Context.MODE_PRIVATE);
        initializeDefaultValues();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_gestures, null);

        imageSwitcher = view.findViewById(R.id.image_switcher);
        imageSwitcher.setFactory(() -> {
            ImageView imageView = new ImageView(requireContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return imageView;
        });
        imageSwitcher.setImageResource(imageResources[currentIndex]);

        imageAssociationSpinner = view.findViewById(R.id.image_association_spinner);
        ArrayAdapter<GestureFunctionalities> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, GestureFunctionalities.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imageAssociationSpinner.setAdapter(adapter);

        // Set spinner value for the current image
        imageAssociationSpinner.setSelection(getSpinnerIndexForImage(labels[currentIndex]));

        nextButton = view.findViewById(R.id.bt_next);
        previousButton = view.findViewById(R.id.bt_previous);

        // Hide previousButton if the index is 0
        updateButtonVisibility();

        nextButton.setOnClickListener(v -> {
            if (currentIndex < imageResources.length - 1) {
                currentIndex++;
                updateImageAndSpinner();
            }
        });

        previousButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateImageAndSpinner();
            }
        });

        builder.setView(view)
                .setTitle("Gesture Settings")
                .setPositiveButton("OK", (dialog, id) -> saveSelections())
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }


    private void initializeDefaultValues() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!sharedPreferences.contains("fist")) {
            editor.putString("fist", GestureFunctionalities.START_RECORDING.name());
        }
        if (!sharedPreferences.contains("ok")) {
            editor.putString("ok", GestureFunctionalities.PAUSE_RECORDING.name());
        }
        if (!sharedPreferences.contains("like")) {
            editor.putString("like", GestureFunctionalities.RESUME_RECORDING.name());
        }
        if (!sharedPreferences.contains("one")) {
            editor.putString("one", GestureFunctionalities.DISABLE.name());
        }
        if (!sharedPreferences.contains("peace")) {
            editor.putString("peace", GestureFunctionalities.DISABLE.name());
        }
        if (!sharedPreferences.contains("palm")) {
            editor.putString("palm", GestureFunctionalities.DISABLE.name());
        }
        if (!sharedPreferences.contains("stop")) {
            editor.putString("stop", GestureFunctionalities.STOP_RECORDING.name());
        }

        editor.apply();
    }

    private int getSpinnerIndexForImage(String label) {
        String savedValue = sharedPreferences.getString(label, GestureFunctionalities.DISABLE.name());
        GestureFunctionalities selectedFunctionality = GestureFunctionalities.valueOf(savedValue);

        return selectedFunctionality.ordinal();
    }

    private void updateImageAndSpinner() {
        imageSwitcher.setImageResource(imageResources[currentIndex]);
        imageAssociationSpinner.setSelection(getSpinnerIndexForImage(labels[currentIndex]));
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        previousButton.setVisibility(currentIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        nextButton.setVisibility(currentIndex == imageResources.length - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    private void saveSelections() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String selectedFunctionality = imageAssociationSpinner.getSelectedItem().toString();
        String currentLabel = labels[currentIndex];
        editor.putString(currentLabel, selectedFunctionality);
        editor.apply();
    }
}
