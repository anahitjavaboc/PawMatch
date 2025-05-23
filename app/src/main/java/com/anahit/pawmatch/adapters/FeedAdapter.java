package com.anahit.pawmatch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Pet;
import com.bumptech.glide.Glide;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private List<Pet> petList;

    public FeedAdapter(List<Pet> petList) {
        this.petList = petList;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_card, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.petNameTextView.setText(pet.getName());
        holder.petAgeTextView.setText(pet.getAge() + " years");
        holder.petBreedTextView.setText(pet.getBreed());
        holder.petBioTextView.setText(pet.getBio());
        Glide.with(holder.itemView.getContext())
                .load(pet.getImageUrl())
                .placeholder(R.drawable.ic_feed) // Fallback icon
                .into(holder.petImageView);
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView petImageView;
        TextView petNameTextView, petAgeTextView, petBreedTextView, petBioTextView;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            petImageView = itemView.findViewById(R.id.pet_image_view);  // ✅ Corrected ID
            petNameTextView = itemView.findViewById(R.id.pet_name_text_view);  // ✅ Corrected ID
            petAgeTextView = itemView.findViewById(R.id.pet_age_text_view);  // ✅ Corrected ID
            petBreedTextView = itemView.findViewById(R.id.pet_breed_text_view);  // ✅ Corrected ID
            petBioTextView = itemView.findViewById(R.id.pet_bio_text_view);  // ✅ Corrected ID
        }
    }

}