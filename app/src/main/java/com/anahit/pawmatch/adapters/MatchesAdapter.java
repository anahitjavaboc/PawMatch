package com.anahit.pawmatch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Pet; // Changed from Match to Pet
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {

    private List<Pet> matchPetList; // Changed to Pet list
    private OnMatchClickListener listener;

    public interface OnMatchClickListener {
        void onMatchClick(Pet pet);
    }

    public MatchesAdapter(List<Pet> matchPetList, OnMatchClickListener listener) {
        this.matchPetList = matchPetList != null ? matchPetList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Pet pet = matchPetList.get(position);

        // Bind pet image
        if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(pet.getImageUrl())
                    .placeholder(R.drawable.pawmatchlogo)
                    .error(R.drawable.pawmatchlogo)
                    .into(holder.petImageView);
        } else {
            holder.petImageView.setImageResource(R.drawable.pawmatchlogo);
        }

        // Bind text fields
        holder.petNameTextView.setText(pet.getName() != null ? pet.getName() : "Unknown Pet");
        holder.ownerNameTextView.setText(pet.getOwnerName() != null ? pet.getOwnerName() : "Unknown Owner");

        // No timestamp or status in Pet model; remove if not needed
        holder.matchTimestamp.setVisibility(View.GONE);
        holder.matchStatus.setVisibility(View.GONE);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMatchClick(pet);
        });
    }

    @Override
    public int getItemCount() {
        return matchPetList != null ? matchPetList.size() : 0;
    }

    public void updateData(List<Pet> newList) {
        matchPetList.clear();
        if (newList != null) {
            matchPetList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        ImageView petImageView;
        TextView petNameTextView;
        TextView ownerNameTextView;
        TextView matchTimestamp;
        TextView matchStatus;

        MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            petImageView = itemView.findViewById(R.id.match_pet_image);
            petNameTextView = itemView.findViewById(R.id.match_pet_name);
            ownerNameTextView = itemView.findViewById(R.id.match_owner_name);
            matchTimestamp = itemView.findViewById(R.id.match_timestamp);
            matchStatus = itemView.findViewById(R.id.match_status);
        }
    }
}