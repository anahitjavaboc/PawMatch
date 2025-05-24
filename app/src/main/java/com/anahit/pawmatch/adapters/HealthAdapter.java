package com.anahit.pawmatch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Pet;
import java.util.List;

public class HealthAdapter extends RecyclerView.Adapter<HealthAdapter.HealthViewHolder> {
    private List<Pet> petList;

    public HealthAdapter(List<Pet> petList) {
        this.petList = petList;
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
        holder.ageTextView.setText("Age: " + (pet.getAge() != null ? pet.getAge() : "Unknown"));
        holder.breedTextView.setText("Breed: " + (pet.getBreed() != null ? pet.getBreed() : "Unknown"));
        holder.bioTextView.setText("Bio: " + (pet.getBio() != null ? pet.getBio() : "Not specified"));
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

        public HealthViewHolder(@NonNull View itemView) {
            super(itemView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            healthStatusTextView = itemView.findViewById(R.id.healthStatusTextView);
            ageTextView = itemView.findViewById(R.id.ageTextView);
            breedTextView = itemView.findViewById(R.id.breedTextView);
            bioTextView = itemView.findViewById(R.id.bioTextView);
        }
    }
}
