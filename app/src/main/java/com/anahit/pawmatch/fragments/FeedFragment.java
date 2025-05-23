package com.anahit.pawmatch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.anahit.pawmatch.R;
import com.anahit.pawmatch.adapters.PetCardAdapter;
import com.anahit.pawmatch.models.Match;
import com.anahit.pawmatch.models.Pet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment implements CardStackListener {
    private CardStackView cardStackView;
    private PetCardAdapter adapter;
    private List<Pet> petList = new ArrayList<>();
    private DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("pets");
    private DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("likes");
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    private ValueEventListener petsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        cardStackView = view.findViewById(R.id.cardStackView);
        if (cardStackView == null) {
            Toast.makeText(requireContext(), "CardStackView not found in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize layout manager and adapter
        CardStackLayoutManager layoutManager = new CardStackLayoutManager(requireContext(), this);
        layoutManager.setDirections(Direction.HORIZONTAL);
        cardStackView.setLayoutManager(layoutManager);

        // Set adapter after initial data load to avoid empty view
        loadPets();

        return view;
    }

    private void loadPets() {
        if (currentUserId == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        petsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                petList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Pet pet = data.getValue(Pet.class);
                    if (pet != null && !pet.getOwnerId().equals(currentUserId)) {
                        pet.setId(data.getKey());
                        petList.add(pet);
                    }
                }
                if (adapter == null) {
                    adapter = new PetCardAdapter(requireContext(), petList);
                    cardStackView.setAdapter(adapter);
                } else {
                    adapter.updateData(petList);
                    adapter.notifyDataSetChanged();
                }

                if (petList.isEmpty()) {
                    Toast.makeText(requireContext(), "No pets available to swipe", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        petsRef.addValueEventListener(petsListener);
    }

    @Override
    public void onCardSwiped(Direction direction) {
        CardStackLayoutManager layoutManager = (CardStackLayoutManager) cardStackView.getLayoutManager();
        int position = layoutManager.getTopPosition() - 1; // Get the swiped card position
        if (position >= 0 && position < petList.size()) {
            Pet pet = petList.get(position);
            petList.remove(position);
            adapter.notifyItemRemoved(position);

            if (direction == Direction.Right && pet != null && currentUserId != null) {
                likesRef.child(currentUserId).child(pet.getId()).child("timestamp")
                        .setValue(System.currentTimeMillis())
                        .addOnSuccessListener(aVoid -> checkForMatch(pet))
                        .addOnFailureListener(e -> {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to like: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void checkForMatch(Pet pet) {
        if (pet != null && pet.getOwnerId() != null && pet.getId() != null && currentUserId != null) {
            likesRef.child(pet.getOwnerId()).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference("matches");
                        String matchId = matchesRef.push().getKey();
                        if (matchId != null) {
                            Match newMatch = new Match(
                                    matchId,
                                    currentUserId,
                                    pet.getOwnerId(),
                                    pet.getId(),
                                    pet.getName(),
                                    null, // ownerName (fetch dynamically if needed)
                                    pet.getImageUrl(), // petImageUrl
                                    System.currentTimeMillis(), // timestamp
                                    "Pending" // status
                            );
                            matchesRef.child(matchId).setValue(newMatch);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Match with " + (pet.getName() != null ? pet.getName() : "Unknown Pet") + "!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {}

    @Override
    public void onCardRewound() {}

    @Override
    public void onCardCanceled() {}

    @Override
    public void onCardAppeared(View view, int position) {}

    @Override
    public void onCardDisappeared(View view, int position) {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (petsListener != null) {
            petsRef.removeEventListener(petsListener);
        }
    }
}