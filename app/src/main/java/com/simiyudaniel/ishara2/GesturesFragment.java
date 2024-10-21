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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;

import java.util.Objects;

public class GesturesFragment extends DialogFragment {
    private ImageSwitcher imageSwitcher;
    private Spinner imageAssociationSpinner;
    private ImageButton nextButton, previousButton;
    private int[] imageResources = {
            R.drawable.fist, R.drawable.ok,
            R.drawable.like, R.drawable.one,
            R.drawable.peace,R.drawable.palm,R.drawable.stop};
    private int currentIndex = 0;
    //
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gestures, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_gestures, null);
        //
        imageSwitcher = view.findViewById(R.id.image_switcher);
        nextButton = view.findViewById(R.id.bt_next);
        previousButton = view.findViewById(R.id.bt_previous);
        imageAssociationSpinner = view.findViewById(R.id.image_association_spinner);
        //
        setupImageSwitcher();

        nextButton.setOnClickListener(v -> {
            if (currentIndex < imageResources.length - 1) {
                currentIndex++;
                imageSwitcher.setImageResource(imageResources[currentIndex]);
                updateSpinnerContent();
            }
        });
        previousButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                imageSwitcher.setImageResource(imageResources[currentIndex]);
                updateSpinnerContent();
            }
        });
        //
        builder.setView(view)
                .setTitle("Gesture Settings")
                .setPositiveButton("OK", (dialog, id) -> {

                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });
        return builder.create();
    }

    private void updateSpinnerContent() {
        String[][] associations = {
                {"Action 1", "Action 2"},
                {"Choice A", "Choice B"},
                {"Select 1", "Select 2"},
                {"Option X", "Option Y"},
                {"Gesture 1", "Gesture 2"},
                {"Gesture A", "Gesture B"},
                {"Command 1", "Command 2"}
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                associations[currentIndex]
        );
        imageAssociationSpinner.setAdapter(adapter);
    }


    private void setupImageSwitcher() {
        imageSwitcher.setFactory(() -> {
            ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return imageView;
        });
        //
        imageSwitcher.setImageResource(imageResources[currentIndex]);
        updateSpinnerContent();
    }
}