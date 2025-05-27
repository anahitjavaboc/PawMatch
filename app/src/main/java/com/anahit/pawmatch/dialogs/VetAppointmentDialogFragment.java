package com.anahit.pawmatch.dialogs;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.anahit.pawmatch.MainActivity;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.ReminderReceiver;
import com.anahit.pawmatch.models.Pet;
import java.util.Map;

public class VetAppointmentDialogFragment extends DialogFragment {
    private Pet pet;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_vet_appointment, null);

        TextView vetAppointmentTextView = view.findViewById(R.id.vetAppointmentTextView);
        if (pet != null && pet.getVetAppointments() != null) {
            StringBuilder appointments = new StringBuilder();
            for (Map.Entry<String, Pet.VetAppointment> entry : pet.getVetAppointments().entrySet()) {
                String apptKey = entry.getKey();
                Pet.VetAppointment appt = entry.getValue();
                appointments.append("Date: ").append(appt.getDate())
                        .append(", Time: ").append(appt.getTime())
                        .append(", Location: ").append(appt.getLocation())
                        .append("\n");
                scheduleReminder(appt.getReminderTimestamp(), apptKey);
            }
            vetAppointmentTextView.setText(appointments.toString().isEmpty() ? "No vet appointments" : appointments.toString());
        } else {
            vetAppointmentTextView.setText("No vet appointments");
        }

        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> dismiss());
        return builder.create();
    }

    private void scheduleReminder(long reminderTimestamp, String apptKey) {
        if (reminderTimestamp <= System.currentTimeMillis()) {
            Log.d("VetAppointment", "Reminder timestamp is in the past, skipping: " + reminderTimestamp);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        intent.putExtra("pet_name", pet.getName());
        intent.putExtra("appt_date", pet.getVetAppointments().get(apptKey).getDate());
        intent.putExtra("appt_key", apptKey);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                apptKey.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimestamp, pendingIntent);
            Log.d("VetAppointment", "Scheduled vet appointment reminder for " + pet.getName() + " at " + reminderTimestamp);
        } catch (SecurityException e) {
            Log.e("VetAppointment", "Failed to schedule alarm: " + e.getMessage(), e);
        }
    }
}