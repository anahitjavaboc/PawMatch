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
import java.util.Map;

public class HealthAdapter extends RecyclerView.Adapter<HealthAdapter.ViewHolder> {
    private List<Pet> pets;
    private HealthFragment fragment;

    public HealthAdapter(List<Pet> pets, HealthFragment fragment) {
        this.pets = pets;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pet pet = pets.get(position);
        holder.petNameTextView.setText(pet.getName() != null ? pet.getName() : "Unknown");
        holder.healthStatusTextView.setText("Health: " + (pet.getHealthStatus() != null ? pet.getHealthStatus() : "Unknown"));
        holder.ageTextView.setText("Age: " + (pet.getAge() > 0 ? pet.getAge() : "Unknown"));
        holder.breedTextView.setText("Breed: " + (pet.getBreed() != null ? pet.getBreed() : "Unknown"));
        holder.bioTextView.setText("Bio: " + (pet.getBio() != null ? pet.getBio() : "Not specified"));

        // Display vet appointments as notes
        StringBuilder vetNotes = new StringBuilder("Vet Appointments:\n");
        if (pet.getVetAppointments() != null && !pet.getVetAppointments().isEmpty()) {
            for (Map.Entry<String, Pet.VetAppointment> entry : pet.getVetAppointments().entrySet()) {
                Pet.VetAppointment appt = entry.getValue();
                vetNotes.append("- Date: ").append(appt.getDate() != null ? appt.getDate() : "N/A")
                        .append(", Time: ").append(appt.getTime() != null ? appt.getTime() : "N/A")
                        .append(", Location: ").append(appt.getLocation() != null ? appt.getLocation() : "N/A")
                        .append("\n");
            }
        } else {
            vetNotes.append("No vet appointments recorded.");
        }
        holder.vetNotesTextView.setText(vetNotes.toString());

        holder.viewVaccinationsButton.setOnClickListener(v -> fragment.onViewVaccinationsClick(pet));
        holder.viewMedicalHistoryButton.setOnClickListener(v -> fragment.onViewMedicalHistoryClick(pet));
        holder.viewMedicationsButton.setOnClickListener(v -> fragment.onViewMedicationsClick(pet));
        holder.viewVetAppointmentsButton.setOnClickListener(v -> fragment.onViewVetAppointmentsClick(pet));
    }

    @Override
    public int getItemCount() {
        return pets != null ? pets.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView petNameTextView, healthStatusTextView, ageTextView, breedTextView, bioTextView, vetNotesTextView;
        Button viewVaccinationsButton, viewMedicalHistoryButton, viewMedicationsButton, viewVetAppointmentsButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            healthStatusTextView = itemView.findViewById(R.id.healthStatusTextView);
            ageTextView = itemView.findViewById(R.id.ageTextView);
            breedTextView = itemView.findViewById(R.id.breedTextView);
            bioTextView = itemView.findViewById(R.id.bioTextView);
            vetNotesTextView = itemView.findViewById(R.id.vetNotesTextView);
            viewVaccinationsButton = itemView.findViewById(R.id.viewVaccinationsButton);
            viewMedicalHistoryButton = itemView.findViewById(R.id.viewMedicalHistoryButton);
            viewMedicationsButton = itemView.findViewById(R.id.viewMedicationsButton);
            viewVetAppointmentsButton = itemView.findViewById(R.id.viewVetAppointmentsButton);
        }
    }
}