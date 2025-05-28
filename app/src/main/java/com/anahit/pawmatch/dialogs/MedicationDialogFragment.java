package com.anahit.pawmatch.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Pet;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class MedicationDialogFragment extends DialogFragment {
    private Pet pet;
    private DatabaseReference petRef;
    private LinearLayout medicationList;

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

        medicationList = view.findViewById(R.id.medicationList);
        EditText nameEditText = view.findViewById(R.id.nameEditText);
        EditText dosageEditText = view.findViewById(R.id.dosageEditText);
        EditText frequencyEditText = view.findViewById(R.id.frequencyEditText);
        CheckBox toBuyCheckBox = view.findViewById(R.id.toBuyCheckBox);
        Button addButton = view.findViewById(R.id.addButton);

        // Set up Firebase reference
        petRef = FirebaseDatabase.getInstance().getReference("pets").child(pet.getId());

        // Validate pet data before proceeding
        if (pet.getOwnerId() == null) {
            Toast.makeText(requireContext(), "Error: Pet owner ID is missing", Toast.LENGTH_SHORT).show();
            dismiss();
            return builder.create();
        }

        // Populate medication list
        if (pet != null && pet.getMedications() != null) {
            for (Map.Entry<String, Pet.Medication> entry : pet.getMedications().entrySet()) {
                addMedicationItem(entry.getKey(), entry.getValue());
            }
        }

        // Add new medication
        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String dosage = dosageEditText.getText().toString().trim();
            String frequency = frequencyEditText.getText().toString().trim();
            boolean toBuy = toBuyCheckBox.isChecked();

            if (name.isEmpty() || dosage.isEmpty() || frequency.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate a unique key for the medication
            String medKey = petRef.child("medications").push().getKey();
            Pet.Medication newMed = new Pet.Medication(dosage, frequency, 0, !toBuy); // Set bought=false if toBuy=true

            // Update Firebase with the new entry
            Map<String, Object> updates = new HashMap<>();
            updates.put("medications/" + medKey, newMed);
            petRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "Medication added", Toast.LENGTH_SHORT).show();
                    // Update the UI
                    if (pet.getMedications() == null) {
                        pet.setMedications(new HashMap<>());
                    }
                    pet.getMedications().put(medKey, newMed);
                    addMedicationItem(medKey, newMed);
                    nameEditText.setText("");
                    dosageEditText.setText("");
                    frequencyEditText.setText("");
                    toBuyCheckBox.setChecked(false);
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Log.e("MedicationDialog", "Failed to add medication: " + errorMsg);
                    Toast.makeText(requireContext(), "Failed to add medication: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> dismiss());
        return builder.create();
    }

    private void addMedicationItem(String medKey, Pet.Medication med) {
        LinearLayout itemLayout = new LinearLayout(requireContext());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        CheckBox boughtCheckBox = new CheckBox(requireContext());
        boughtCheckBox.setChecked(med.isBought());
        boughtCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            med.setBought(isChecked);
            Map<String, Object> updates = new HashMap<>();
            updates.put("medications/" + medKey + "/bought", isChecked);
            petRef.updateChildren(updates);
        });

        TextView medTextView = new TextView(requireContext());
        medTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        medTextView.setText("Medication: " + medKey + "\nDosage: " + med.getDosage() + "\nFrequency: " + med.getFrequency());
        medTextView.setPadding(8, 8, 8, 8);

        itemLayout.addView(boughtCheckBox);
        itemLayout.addView(medTextView);
        medicationList.addView(itemLayout);
    }
}