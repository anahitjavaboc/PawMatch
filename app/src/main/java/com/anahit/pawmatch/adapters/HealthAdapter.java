package com.anahit.pawmatch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.fragments.HealthFragment;
import com.anahit.pawmatch.models.Pet;
import java.util.List;

public class HealthAdapter extends RecyclerView.Adapter<HealthAdapter.HealthViewHolder> {
    private List<Pet> petList;
    private HealthFragment fragment;

    public HealthAdapter(List<Pet> petList, HealthFragment fragment) {
        this.petList = petList;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public HealthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health, parent, false);
        return new HealthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthViewHolder holder, int position) {
        Pet pet = petList.get(position);

        holder.petNameTextView.setText(pet.getName() != null ? pet.getName() : "Unknown Pet");
        holder.healthStatusTextView.setText("Health: " + (pet.getHealthStatus() != null ? pet.getHealthStatus() : "Not specified"));
        holder.ageTextView.setText("Age: " + (pet.getAge() > 0 ? pet.getAge() : "Unknown"));
        holder.breedTextView.setText("Breed: " + (pet.getBreed() != null ? pet.getBreed() : "Unknown"));
        holder.bioTextView.setText("Bio: " + (pet.getBio() != null ? pet.getBio() : "Not specified"));

        // Set button click listeners
        holder.viewVaccinationsButton.setOnClickListener(v -> {
            if (fragment != null) fragment.onViewVaccinationsClick(pet);
        });
        holder.viewMedicalHistoryButton.setOnClickListener(v -> {
            if (fragment != null) fragment.onViewMedicalHistoryClick(pet);
        });
        holder.viewMedicationsButton.setOnClickListener(v -> {
            if (fragment != null) fragment.onViewMedicationsClick(pet);
        });
        holder.viewVetAppointmentsButton.setOnClickListener(v -> {
            if (fragment != null) fragment.onViewVetAppointmentsClick(pet);
        });
    }

    @Override
    public int getItemCount() {
        return petList != null ? petList.size() : 0;
    }

    public static class HealthViewHolder extends RecyclerView.ViewHolder {
        TextView petNameTextView;
        TextView healthStatusTextView;
        TextView ageTextView;
        TextView breedTextView;
        TextView bioTextView;
        Button viewVaccinationsButton;
        Button viewMedicalHistoryButton;
        Button viewMedicationsButton;
        Button viewVetAppointmentsButton;

        public HealthViewHolder(@NonNull View itemView) {
            super(itemView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            healthStatusTextView = itemView.findViewById(R.id.healthStatusTextView);
            ageTextView = itemView.findViewById(R.id.ageTextView);
            breedTextView = itemView.findViewById(R.id.breedTextView);
            bioTextView = itemView.findViewById(R.id.bioTextView);
            viewVaccinationsButton = itemView.findViewById(R.id.viewVaccinationsButton);
            viewMedicalHistoryButton = itemView.findViewById(R.id.viewMedicalHistoryButton);
            viewMedicationsButton = itemView.findViewById(R.id.viewMedicationsButton);
            viewVetAppointmentsButton = itemView.findViewById(R.id.viewVetAppointmentsButton);
        }
    }
}