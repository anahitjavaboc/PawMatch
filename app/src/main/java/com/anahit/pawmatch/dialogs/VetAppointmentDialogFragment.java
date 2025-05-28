package com.anahit.pawmatch.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import com.anahit.pawmatch.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VetAppointmentDialogFragment extends DialogFragment {
    private Pet pet;
    private DatabaseReference petRef;

    public static VetAppointmentDialogFragment newInstance(Pet pet) {
        VetAppointmentDialogFragment fragment = new VetAppointmentDialogFragment();
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
            Log.e("VetAppointmentDialog", "Pet object is null");
            Toast.makeText(requireContext(), "Pet data unavailable", Toast.LENGTH_SHORT).show();
            dismiss();
            return null;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e("VetAppointmentDialog", "User not authenticated");
            Toast.makeText(requireContext(), "Please sign in to add appointments", Toast.LENGTH_SHORT).show();
            dismiss();
            return null;
        }

        String userId = auth.getCurrentUser().getUid();
        Log.d("VetAppointmentDialog", "User UID: " + userId + ", Pet ownerId: " + pet.getOwnerId());
        if (!userId.equals(pet.getOwnerId())) {
            Log.e("VetAppointmentDialog", "Permission denied: User UID does not match pet ownerId");
            Toast.makeText(requireContext(), "Permission denied: You can only edit your own pet's data", Toast.LENGTH_LONG).show();
            dismiss();
            return null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_vet_appointment, null);

        TextView vetAppointmentTextView = view.findViewById(R.id.vetAppointmentTextView);
        EditText yearEditText = view.findViewById(R.id.yearEditText);
        EditText monthEditText = view.findViewById(R.id.monthEditText);
        EditText dayEditText = view.findViewById(R.id.dayEditText);
        EditText timeEditText = view.findViewById(R.id.timeEditText);
        EditText locationEditText = view.findViewById(R.id.locationEditText);
        Button addButton = view.findViewById(R.id.addButton);

        // Set up Firebase reference
        petRef = FirebaseDatabase.getInstance().getReference("pets").child(pet.getId());

        // Display current vet appointments
        if (pet != null && pet.getVetAppointments() != null && !pet.getVetAppointments().isEmpty()) {
            StringBuilder appointments = new StringBuilder();
            for (Map.Entry<String, Pet.VetAppointment> entry : pet.getVetAppointments().entrySet()) {
                Pet.VetAppointment appt = entry.getValue();
                appointments.append("Date: ").append(appt.getDate())
                        .append(", Time: ").append(appt.getTime())
                        .append(", Location: ").append(appt.getLocation())
                        .append("\n");
            }
            vetAppointmentTextView.setText(appointments.toString());
        } else {
            vetAppointmentTextView.setText("No vet appointments available.");
        }

        // Add new vet appointment
        addButton.setOnClickListener(v -> {
            String year = yearEditText.getText().toString().trim();
            String month = monthEditText.getText().toString().trim();
            String day = dayEditText.getText().toString().trim();
            String timeInput = timeEditText.getText().toString().trim();
            String location = locationEditText.getText().toString().trim();

            if (year.isEmpty() || month.isEmpty() || day.isEmpty() || timeInput.isEmpty() || location.isEmpty()) {
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

            // Validate and convert time from HHMM to HH:MM
            if (!timeInput.matches("\\d{4}")) {
                Toast.makeText(requireContext(), "Time must be in HHMM format (e.g., 1430)", Toast.LENGTH_SHORT).show();
                return;
            }

            String time;
            try {
                time = timeInput.substring(0, 2) + ":" + timeInput.substring(2, 4);
                if (!time.matches("\\d{2}:\\d{2}")) {
                    throw new IllegalArgumentException("Invalid time format");
                }
                int hours = Integer.parseInt(time.substring(0, 2));
                int minutes = Integer.parseInt(time.substring(3, 5));
                if (hours > 23 || minutes > 59) {
                    throw new IllegalArgumentException("Invalid time (HH must be 00-23, MM must be 00-59)");
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Invalid time format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate reminder timestamp (1 day before the appointment)
            long reminderTimestamp;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                Date apptDate = sdf.parse(date + " " + time);
                reminderTimestamp = apptDate.getTime() - (24 * 60 * 60 * 1000); // 1 day before
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate a unique key for the appointment
            String apptKey = petRef.child("vetAppointments").push().getKey();
            Pet.VetAppointment newAppt = new Pet.VetAppointment(date, time, location, reminderTimestamp);

            // Update Firebase with the new entry
            Map<String, Object> updates = new HashMap<>();
            updates.put("vetAppointments/" + apptKey, newAppt);
            petRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "Vet appointment added", Toast.LENGTH_SHORT).show();
                    // Update the UI
                    if (pet.getVetAppointments() == null) {
                        pet.setVetAppointments(new HashMap<>());
                    }
                    pet.getVetAppointments().put(apptKey, newAppt);
                    StringBuilder updatedAppointments = new StringBuilder();
                    for (Map.Entry<String, Pet.VetAppointment> entry : pet.getVetAppointments().entrySet()) {
                        Pet.VetAppointment appt = entry.getValue();
                        updatedAppointments.append("Date: ").append(appt.getDate())
                                .append(", Time: ").append(appt.getTime())
                                .append(", Location: ").append(appt.getLocation())
                                .append("\n");
                    }
                    vetAppointmentTextView.setText(updatedAppointments.toString());
                    yearEditText.setText("");
                    monthEditText.setText("");
                    dayEditText.setText("");
                    timeEditText.setText("");
                    locationEditText.setText("");

                    // Schedule notification
                    NotificationHelper.createNotificationChannel(requireContext());
                    boolean scheduled = NotificationHelper.scheduleNotification(requireContext(), reminderTimestamp, pet.getName(), location);
                    if (!scheduled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Toast.makeText(requireContext(), "Please enable exact alarm permission in settings to receive reminders.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intent);
                    }
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Log.e("VetAppointmentDialog", "Failed to add appointment: " + errorMsg);
                    Toast.makeText(requireContext(), "Failed to add vet appointment: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> dismiss());
        return builder.create();
    }
}