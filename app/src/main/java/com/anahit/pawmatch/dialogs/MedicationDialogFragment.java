package com.anahit.pawmatch.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Pet;
import java.util.Map;

public class MedicationDialogFragment extends DialogFragment {
    private Pet pet;

    public static MedicationDialogFragment newInstance(Pet pet) {
        MedicationDialogFragment fragment = new MedicationDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("pet", pet);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            pet = (Pet) getArguments().getSerializable("pet");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_medication, null);

        TextView medicationTextView = view.findViewById(R.id.medicationTextView);
        if (pet != null && pet.getMedications() != null && !pet.getMedications().isEmpty()) {
            StringBuilder medications = new StringBuilder();
            for (Map.Entry<String, Pet.Medication> entry : pet.getMedications().entrySet()) {
                Pet.Medication med = entry.getValue();
                medications.append("Medication: ").append(entry.getKey())
                        .append("\nDosage: ").append(med.getDosage())
                        .append("\nFrequency: ").append(med.getFrequency())
                        .append("\n\n");
            }
            medicationTextView.setText(medications.toString());
        } else {
            medicationTextView.setText("No medications recorded.");
        }

        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> dismiss());
        return builder.create();
    }
}