package com.anahit.pawmatch.fragments;

import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "FeedFragment";
    private CardStackView cardStackView;
    private PetCardAdapter adapter;
    private List<Pet> petList = new ArrayList<>();
    private DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("pets");
    private DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("likes");
    private DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference("matches");
    private String currentUserId;
    private ValueEventListener petsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        cardStackView = view.findViewById(R.id.cardStackView);
        if (cardStackView == null) {
            Toast.makeText(requireContext(), "CardStackView not found in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize current user
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize CardStackView
        CardStackLayoutManager layoutManager = new CardStackLayoutManager(requireContext(), this);
        layoutManager.setDirections(Direction.HORIZONTAL); // Allow left and right swipes
        layoutManager.setSwipeThreshold(0.3f); // Adjust swipe sensitivity
        layoutManager.setMaxDegree(20.0f); // Slight rotation for aesthetic
        layoutManager.setTranslationInterval(8.0f); // Smooth movement
        cardStackView.setLayoutManager(layoutManager);

        // Load pets and set adapter
        loadPets();

        return view;
    }

    private void loadPets() {
        petsListener = petsRef.addValueEventListener(new ValueEventListener() {
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
                if (!petList.isEmpty()) {
                    if (adapter == null) {
                        adapter = new PetCardAdapter(requireContext(), petList);
                        cardStackView.setAdapter(adapter);
                    } else {
                        adapter.updateData(petList);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(requireContext(), "No pets available to swipe", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load pets: " + error.getMessage());
                Toast.makeText(requireContext(), "Error loading pets: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (adapter == null || petList.isEmpty()) return;

        int position = ((CardStackLayoutManager) cardStackView.getLayoutManager()).getTopPosition() - 1;
        if (position < 0 || position >= petList.size()) return;

        Pet pet = petList.get(position);
        petList.remove(position);

        if (direction == Direction.Right) {
            handleLike(pet);
        } else if (direction == Direction.Left) {
            Toast.makeText(requireContext(), "Skipped " + (pet.getName() != null ? pet.getName() : "pet"), Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged(); // Refresh the card stack
    }

    private void handleLike(Pet pet) {
        if (pet == null || pet.getId() == null || currentUserId == null) return;

        likesRef.child(currentUserId).child(pet.getId()).child("timestamp")
                .setValue(System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> checkForMatch(pet))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to like: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to like", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkForMatch(Pet pet) {
        if (pet == null || pet.getOwnerId() == null || pet.getId() == null) return;

        likesRef.child(pet.getOwnerId()).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String matchId = matchesRef.push().getKey();
                    if (matchId != null) {
                        Match match = new Match(
                                matchId,
                                currentUserId,
                                pet.getOwnerId(),
                                pet.getId(),
                                pet.getName(),
                                null, // ownerName (fetch dynamically if needed)
                                pet.getImageUrl(),
                                System.currentTimeMillis(),
                                "Pending"
                        );
                        matchesRef.child(matchId).setValue(match)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "Match with " + (pet.getName() != null ? pet.getName() : "Unknown Pet") + "!", Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create match: " + e.getMessage());
                                    Toast.makeText(requireContext(), "Failed to create match", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(requireContext(), "Liked " + (pet.getName() != null ? pet.getName() : "pet") + "!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Match check failed: " + error.getMessage());
                Toast.makeText(requireContext(), "Error checking match", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {
        // Optional: Add animation or feedback during drag
    }

    @Override
    public void onCardRewound() {
        // Not implemented (rewind feature optional)
    }

    @Override
    public void onCardCanceled() {
        // Not implemented (cancel swipe optional)
    }

    @Override
    public void onCardAppeared(View view, int position) {
        // Not implemented (card appearance optional)
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        // Not implemented (card disappearance optional)
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (petsListener != null) {
            petsRef.removeEventListener(petsListener);
        }
    }
}