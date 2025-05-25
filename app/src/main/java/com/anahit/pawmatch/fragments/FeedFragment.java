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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedFragment extends Fragment implements CardStackListener {

    private static final String TAG = "FeedFragment";

    private CardStackView cardStackView;
    private PetCardAdapter adapter;
    private List<Pet> petList = new ArrayList<>();
    private DatabaseReference petsRef;
    private DatabaseReference matchesRef;
    private ValueEventListener petsListener;
    private CardStackLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        cardStackView = view.findViewById(R.id.card_stack_view);
        if (cardStackView == null) {
            Toast.makeText(requireContext(), "CardStackView not found in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        matchesRef = FirebaseDatabase.getInstance().getReference("matches");

        setupCardStackView();
        loadPets();

        return view;
    }

    private void setupCardStackView() {
        layoutManager = new CardStackLayoutManager(requireContext(), this);
        SwipeAnimationSetting swipeRightSetting = new SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .build();
        layoutManager.setSwipeAnimationSetting(swipeRightSetting);
        layoutManager.setDirections(Direction.HORIZONTAL);
        layoutManager.setSwipeThreshold(0.3f);
        layoutManager.setMaxDegree(20.0f);
        layoutManager.setTranslationInterval(8.0f);

        cardStackView.setLayoutManager(layoutManager);
        adapter = new PetCardAdapter(requireContext(), petList);
        cardStackView.setAdapter(adapter);
    }

    private void loadPets() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to swipe pets", Toast.LENGTH_LONG).show();
            return;
        }
        String currentUserId = currentUser.getUid();
        Log.d(TAG, "Loading pets for user: " + currentUserId);

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
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Loaded " + petList.size() + " pets");

                if (petList.isEmpty()) {
                    Toast.makeText(requireContext(), "No pets available to swipe", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading pets: " + error.getMessage(), error.toException());
                Toast.makeText(requireContext(), "Error loading pets: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        petsRef.addValueEventListener(petsListener);
    }

    private void saveMatch(Pet likedPet) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to save matches", Toast.LENGTH_LONG).show();
            return;
        }
        String currentUserId = currentUser.getUid();
        String matchId = matchesRef.push().getKey();
        if (matchId == null) {
            Toast.makeText(requireContext(), "Error generating match ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Match match = new Match(
                matchId,
                currentUserId,
                likedPet.getId(),
                likedPet.getOwnerId(),
                likedPet.getName(),
                likedPet.getOwnerName(), // Ensure this is set if available
                likedPet.getImageUrl(),
                System.currentTimeMillis(),
                "pending"
        );

        Log.d(TAG, "Saving match: " + match.toString());
        matchesRef.child(matchId).setValue(match)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Match saved with ID: " + matchId);
                    Toast.makeText(requireContext(), "Match saved!", Toast.LENGTH_SHORT).show();
                    checkMutualMatch(matchId, likedPet.getOwnerId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save match: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Failed to save match: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkMutualMatch(String matchId, String petOwnerId) {
        matchesRef.orderByChild("petOwnerId").equalTo(petOwnerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser == null) return;
                        String currentUserId = currentUser.getUid();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            String otherMatchUserId = data.child("userId").getValue(String.class);
                            String otherMatchPetOwnerId = data.child("petOwnerId").getValue(String.class);
                            if (otherMatchUserId != null && otherMatchPetOwnerId != null &&
                                    otherMatchUserId.equals(petOwnerId) && otherMatchPetOwnerId.equals(currentUserId)) {
                                // Mutual match found
                                matchesRef.child(matchId).child("status").setValue("matched")
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Mutual match confirmed for ID: " + matchId);
                                            Toast.makeText(requireContext(), "Mutual match found!", Toast.LENGTH_LONG).show();
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update match status: " + e.getMessage()));
                                data.getRef().child("status").setValue("matched"); // Update the other match
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Mutual match check cancelled: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {}

    @Override
    public void onCardSwiped(Direction direction) {
        int position = layoutManager.getTopPosition() - 1;
        if (position >= 0 && position < petList.size()) {
            Pet swipedPet = petList.get(position);
            petList.remove(position);
            adapter.notifyItemRemoved(position);

            if (direction == Direction.Right) {
                Log.d(TAG, "Liked pet: " + (swipedPet.getName() != null ? swipedPet.getName() : "Unknown Pet"));
                Toast.makeText(requireContext(), "Liked " + (swipedPet.getName() != null ? swipedPet.getName() : "Unknown Pet"), Toast.LENGTH_SHORT).show();
                saveMatch(swipedPet);
            } else if (direction == Direction.Left) {
                Log.d(TAG, "Passed pet: " + (swipedPet.getName() != null ? swipedPet.getName() : "Unknown Pet"));
                Toast.makeText(requireContext(), "Passed " + (swipedPet.getName() != null ? swipedPet.getName() : "Unknown Pet"), Toast.LENGTH_SHORT).show();
            }

            if (petList.isEmpty()) {
                Toast.makeText(requireContext(), "No more pets to swipe!", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Invalid swipe position: " + position);
        }
    }

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