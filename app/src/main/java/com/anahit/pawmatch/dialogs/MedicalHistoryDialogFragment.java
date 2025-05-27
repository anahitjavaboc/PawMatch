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

public class MedicalHistoryDialogFragment extends DialogFragment {
    private Pet pet;

    public static MedicalHistoryDialogFragment newInstance(Pet pet) {
        MedicalHistoryDialogFragment fragment = new MedicalHistoryDialogFragment();
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
        View view = inflater.inflate(R.layout.dialog_medical_history, null);

        TextView medicalHistoryTextView = view.findViewById(R.id.medicalHistoryTextView);
        if (pet != null && pet.getMedicalHistory() != null) {
            StringBuilder history = new StringBuilder();
            for (Map.Entry<String, String> entry : pet.getMedicalHistory().entrySet()) {
                history.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            medicalHistoryTextView.setText(history.toString().isEmpty() ? "No medical history" : history.toString());
        } else {
            medicalHistoryTextView.setText("No medical history");
        }

        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> dismiss());
        return builder.create();
    }
}