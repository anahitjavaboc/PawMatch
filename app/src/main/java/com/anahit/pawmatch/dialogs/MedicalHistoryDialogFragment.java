package com.anahit.pawmatch.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Pet;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class MedicalHistoryDialogFragment extends DialogFragment {
    private Pet pet;
    private DatabaseReference petRef;

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
        EditText yearEditText = view.findViewById(R.id.yearEditText);
        EditText monthEditText = view.findViewById(R.id.monthEditText);
        EditText dayEditText = view.findViewById(R.id.dayEditText);
        EditText descriptionEditText = view.findViewById(R.id.descriptionEditText);
        Button addButton = view.findViewById(R.id.addButton);

        // Set up Firebase reference
        petRef = FirebaseDatabase.getInstance().getReference("pets").child(pet.getId());

        // Display current medical history
        if (pet != null && pet.getMedicalHistory() != null && !pet.getMedicalHistory().isEmpty()) {
            StringBuilder history = new StringBuilder();
            for (Map.Entry<String, String> entry : pet.getMedicalHistory().entrySet()) {
                history.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            medicalHistoryTextView.setText(history.toString());
        } else {
            medicalHistoryTextView.setText("No medical history available.");
        }

        // Add new medical history entry
        addButton.setOnClickListener(v -> {
            String year = yearEditText.getText().toString().trim();
            String month = monthEditText.getText().toString().trim();
            String day = dayEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (year.isEmpty() || month.isEmpty() || day.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate date
            int yearInt, monthInt, dayInt;
            try {
                yearInt = Integer.parseInt(year);
                monthInt = Integer.parseInt(month);
                dayInt = Integer.parseInt(day);
                if (yearInt < 1900 || yearInt > 9999 || monthInt < 1 || monthInt > 12 || dayInt < 1 || dayInt > 31) {
                    throw new IllegalArgumentException("Invalid date values");
                }
                // Basic day validation (simplified, assumes 30/31 days per month)
                int maxDays = (monthInt == 4 || monthInt == 6 || monthInt == 9 || monthInt == 11) ? 30 :
                        (monthInt == 2) ? (yearInt % 4 == 0 && (yearInt % 100 != 0 || yearInt % 400 == 0) ? 29 : 28) : 31;
                if (dayInt > maxDays) {
                    throw new IllegalArgumentException("Invalid day for the given month");
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Invalid date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            String date = String.format("%04d-%02d-%02d", yearInt, monthInt, dayInt);

            // Update Firebase with the new entry
            Map<String, Object> updates = new HashMap<>();
            updates.put("medicalHistory/" + date, description);
            petRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "Medical history added", Toast.LENGTH_SHORT).show();
                    // Update the UI
                    if (pet.getMedicalHistory() == null) {
                        pet.setMedicalHistory(new HashMap<>());
                    }
                    pet.getMedicalHistory().put(date, description);
                    StringBuilder updatedHistory = new StringBuilder();
                    for (Map.Entry<String, String> entry : pet.getMedicalHistory().entrySet()) {
                        updatedHistory.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                    medicalHistoryTextView.setText(updatedHistory.toString());
                    yearEditText.setText("");
                    monthEditText.setText("");
                    dayEditText.setText("");
                    descriptionEditText.setText("");
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Log.e("MedicalHistoryDialog", "Failed to add medical history: " + errorMsg);
                    Toast.makeText(requireContext(), "Failed to add medical history: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> dismiss());
        return builder.create();
    }
}