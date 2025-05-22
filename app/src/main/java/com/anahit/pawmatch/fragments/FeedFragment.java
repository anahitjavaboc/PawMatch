package com.anahit.pawmatch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.anahit.pawmatch.R;
import com.anahit.pawmatch.adapters.PetCardAdapter;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        cardStackView = view.findViewById(R.id.cardStackView);

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

        petsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                petList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Pet pet = data.getValue(Pet.class);
                    if (pet != null && !pet.getOwnerId().equals(currentUserId)) {
                        petList.add(pet);
                    }
                }
                // Initialize adapter only if not already set
                if (adapter == null) {
                    adapter = new PetCardAdapter(petList);
                    cardStackView.setAdapter(adapter);
                } else {
                    adapter.updateData(petList); // Assuming PetCardAdapter has an update method
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (direction == Direction.Right && !petList.isEmpty()) {
            // Use the top card (position 0) since we don’t have the exact position
            Pet pet = petList.get(0);
            if (pet != null && currentUserId != null) {
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
        if (pet != null && pet.getOwnerId() != null && currentUserId != null) {
            likesRef.child(pet.getOwnerId()).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference("matches");
                        String matchId = matchesRef.push().getKey();
                        matchesRef.child(matchId).child("userId1").setValue(currentUserId);
                        matchesRef.child(matchId).child("userId2").setValue(pet.getOwnerId());
                        matchesRef.child(matchId).child("petId").setValue(pet.getId());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Match with " + pet.getName() + "!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {
        // Optional: Add dragging feedback if needed
    }

    @Override
    public void onCardRewound() {
        // Optional: Handle card rewind if needed
    }

    @Override
    public void onCardCanceled() {
        // Optional: Handle canceled swipe if needed
    }

    @Override
    public void onCardAppeared(View view, int position) {
        // Optional: Handle card appearance if needed
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        // Optional: Handle card disappearance if needed
    }
}