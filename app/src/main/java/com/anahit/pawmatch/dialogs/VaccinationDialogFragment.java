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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VaccinationDialogFragment extends DialogFragment {
    private Pet pet;
    private DatabaseReference petRef;

    public static VaccinationDialogFragment newInstance(Pet pet) {
        VaccinationDialogFragment fragment = new VaccinationDialogFragment();
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

        if (pet == null) {
            Log.e("VaccinationDialog", "Pet object is null");
            Toast.makeText(requireContext(), "Pet data unavailable", Toast.LENGTH_SHORT).show();
            dismiss();
            return null;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e("VaccinationDialog", "User not authenticated");
            Toast.makeText(requireContext(), "Please sign in to add vaccinations", Toast.LENGTH_SHORT).show();
            dismiss();
            return null;
        }

        String userId = auth.getCurrentUser().getUid();
        Log.d("VaccinationDialog", "User UID: " + userId + ", Pet ownerId: " + pet.getOwnerId());
        if (!userId.equals(pet.getOwnerId())) {
            Log.e("VaccinationDialog", "Permission denied: User UID does not match pet ownerId");
            Toast.makeText(requireContext(), "Permission denied: You can only edit your own pet's data", Toast.LENGTH_LONG).show();
            dismiss();
            return null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_vaccination, null);

        TextView vaccinationTextView = view.findViewById(R.id.vaccinationTextView);
        EditText yearEditText = view.findViewById(R.id.yearEditText);
        EditText monthEditText = view.findViewById(R.id.monthEditText);
        EditText dayEditText = view.findViewById(R.id.dayEditText);
        EditText typeEditText = view.findViewById(R.id.typeEditText);
        Button addButton = view.findViewById(R.id.addButton);

        petRef = FirebaseDatabase.getInstance().getReference("pets").child(pet.getId());

        if (pet != null && pet.getVaccinationRecords() != null && !pet.getVaccinationRecords().isEmpty()) {
            StringBuilder vaccinations = new StringBuilder();
            for (Map.Entry<String, Pet.Vaccination> entry : pet.getVaccinationRecords().entrySet()) {
                Pet.Vaccination vac = entry.getValue();
                vaccinations.append("Date: ").append(vac.getDate())
                        .append(", Type: ").append(vac.getType())
                        .append("\n");
            }
            vaccinationTextView.setText(vaccinations.toString());
        } else {
            vaccinationTextView.setText("No vaccination records available.");
        }

        addButton.setOnClickListener(v -> {
            String year = yearEditText.getText().toString().trim();
            String month = monthEditText.getText().toString().trim();
            String day = dayEditText.getText().toString().trim();
            String type = typeEditText.getText().toString().trim();

            if (year.isEmpty() || month.isEmpty() || day.isEmpty() || type.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate date (similar to VetAppointmentDialog)
            int yearInt, monthInt, dayInt;
            try {
                yearInt = Integer.parseInt(year);
                monthInt = Integer.parseInt(month);
                dayInt = Integer.parseInt(day);
                if (yearInt < 1900 || yearInt > 9999 || monthInt < 1 || monthInt > 12 || dayInt < 1 || dayInt > 31) {
                    throw new IllegalArgumentException("Invalid date values");
                }
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
            long reminderTimestamp = 0; // Set to 0 if not upcoming, or calculate if needed

            String vacKey = petRef.child("vaccinationRecords").push().getKey();
            Pet.Vaccination newVac = new Pet.Vaccination(date, type, false, reminderTimestamp);

            Map<String, Object> updates = new HashMap<>();
            updates.put("vaccinationRecords/" + vacKey, newVac);
            petRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "Vaccination added", Toast.LENGTH_SHORT).show();
                    if (pet.getVaccinationRecords() == null) {
                        pet.setVaccinationRecords(new HashMap<>());
                    }
                    pet.getVaccinationRecords().put(vacKey, newVac);
                    StringBuilder updatedVaccinations = new StringBuilder();
                    for (Map.Entry<String, Pet.Vaccination> entry : pet.getVaccinationRecords().entrySet()) {
                        Pet.Vaccination vac = entry.getValue();
                        updatedVaccinations.append("Date: ").append(vac.getDate())
                                .append(", Type: ").append(vac.getType())
                                .append("\n");
                    }
                    vaccinationTextView.setText(updatedVaccinations.toString());
                    yearEditText.setText("");
                    monthEditText.setText("");
                    dayEditText.setText("");
                    typeEditText.setText("");
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Log.e("VaccinationDialog", "Failed to add vaccination: " + errorMsg);
                    Toast.makeText(requireContext(), "Failed to add vaccination: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> dismiss());
        return builder.create();
    }
}