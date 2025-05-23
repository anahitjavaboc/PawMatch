package com.anahit.pawmatch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Pet;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PetCardAdapter extends RecyclerView.Adapter<PetCardAdapter.PetViewHolder> {
    private Context context;
    private List<Pet> petList;

    public PetCardAdapter(Context context, List<Pet> petList) {
        this.context = context;
        this.petList = petList != null ? petList : new ArrayList<>();
    }

    @Override
    public PetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet_card, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PetViewHolder holder, int position) {
        if (petList == null || position < 0 || position >= petList.size()) return;

        Pet pet = petList.get(position);
        holder.nameTextView.setText(pet.getName() != null ? pet.getName() : "Unknown Pet");
        holder.healthTextView.setText("Health: " + (pet.getHealthStatus() != null ? pet.getHealthStatus() : "Not specified"));
        holder.ageTextView.setText("Age: " + (pet.getAge() != null ? pet.getAge() : "Unknown"));
        holder.breedTextView.setText("Breed: " + (pet.getBreed() != null ? pet.getBreed() : "Unknown"));
        holder.bioTextView.setText("Bio: " + (pet.getBio() != null ? pet.getBio() : "Not specified"));

        if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(pet.getImageUrl())
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .error(R.drawable.ic_pet_placeholder)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_pet_placeholder);
        }
    }

    public void updateData(List<Pet> newList) {
        if (newList != null) {
            petList.clear();
            petList.addAll(newList);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return petList != null ? petList.size() : 0;
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView healthTextView;
        TextView ageTextView;
        TextView breedTextView;
        TextView bioTextView;

        public PetViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pet_image_view);
            nameTextView = itemView.findViewById(R.id.pet_name_text_view);
            healthTextView = itemView.findViewById(R.id.pet_health_text_view);
            ageTextView = itemView.findViewById(R.id.pet_age_text_view);
            breedTextView = itemView.findViewById(R.id.pet_breed_text_view);
            bioTextView = itemView.findViewById(R.id.pet_bio_text_view);
        }
    }
}