package com.anahit.pawmatch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Pet;

import java.util.List;

public class HealthAdapter extends RecyclerView.Adapter<HealthAdapter.HealthViewHolder> {
    private List<Pet> pets;

    public HealthAdapter(List<Pet> pets) {
        this.pets = pets;
    }

    @Override
    public HealthViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health, parent, false);
        return new HealthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HealthViewHolder holder, int position) {
        Pet pet = pets.get(position);
        holder.petNameTextView.setText(pet.getName());
        holder.healthStatusTextView.setText("Health: " + (pet.getHealthStatus() != null ? pet.getHealthStatus() : "Not specified"));
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    public static class HealthViewHolder extends RecyclerView.ViewHolder {
        TextView petNameTextView;
        TextView healthStatusTextView;

        public HealthViewHolder(View itemView) {
            super(itemView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            healthStatusTextView = itemView.findViewById(R.id.healthStatusTextView);
        }
    }
}