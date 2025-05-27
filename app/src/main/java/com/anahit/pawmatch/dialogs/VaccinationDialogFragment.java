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

public class VaccinationDialogFragment extends DialogFragment {
    private Pet pet;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_vaccination, null);

        TextView vaccinationTextView = view.findViewById(R.id.vaccinationTextView);
        if (pet != null && pet.getVaccinationRecords() != null) {
            StringBuilder vaccinations = new StringBuilder();
            for (Map.Entry<String, Pet.Vaccination> entry : pet.getVaccinationRecords().entrySet()) {
                Pet.Vaccination vac = entry.getValue();
                vaccinations.append("Type: ").append(vac.getType())
                        .append(", Date: ").append(vac.getDate())
                        .append(", Upcoming: ").append(vac.isUpcoming())
                        .append("\n");
            }
            vaccinationTextView.setText(vaccinations.toString().isEmpty() ? "No vaccination records" : vaccinations.toString());
        } else {
            vaccinationTextView.setText("No vaccination records");
        }

        builder.setView(view)
                .setPositiveButton("Close", (dialog, id) -> dismiss());
        return builder.create();
    }
}