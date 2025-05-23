package com.anahit.pawmatch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Match;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {

    private List<Match> matchList;
    private OnMatchClickListener listener;

    public interface OnMatchClickListener {
        void onMatchClick(Match match);
    }

    public MatchesAdapter(List<Match> matchList, OnMatchClickListener listener) {
        this.matchList = matchList != null ? matchList : new ArrayList<>();
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
        Match match = matchList.get(position);

        // Bind pet image
        if (match.getPetImageUrl() != null && !match.getPetImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(match.getPetImageUrl())
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .error(R.drawable.ic_pet_placeholder)
                    .into(holder.petImageView);
        } else {
            holder.petImageView.setImageResource(R.drawable.ic_pet_placeholder);
        }

        // Bind text fields
        holder.petNameTextView.setText(match.getPetName() != null ? match.getPetName() : "Unknown Pet");
        holder.ownerNameTextView.setText(match.getOwnerName() != null ? match.getOwnerName() : "Unknown Owner");

        // Format and bind timestamp
        String timestampStr = "Unknown time";
        if (match.getTimestamp() != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            timestampStr = sdf.format(new Date(match.getTimestamp()));
        }
        holder.matchTimestamp.setText(timestampStr);

        // Bind status
        holder.matchStatus.setText("Status: " + (match.getStatus() != null ? match.getStatus() : "Pending"));

        // Set click listener
        holder.itemView.setOnClickListener(v -> listener.onMatchClick(match));
    }

    @Override
    public int getItemCount() {
        return matchList != null ? matchList.size() : 0;
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