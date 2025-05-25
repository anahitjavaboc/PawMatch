package com.anahit.pawmatch.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.adapters.PetCardAdapter;
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

public class SwipeFragment extends Fragment {

    private static final String TAG = "SwipeFragment";

    private CardStackView cardStackView;
    private PetCardAdapter adapter;
    private List<Pet> petList = new ArrayList<>();
    private DatabaseReference petsRef;
    private DatabaseReference matchesRef;
    private ValueEventListener petsListener;
    private CardStackLayoutManager layoutManager;
    private ImageView swipeLeftIcon, swipeRightIcon;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        cardStackView = view.findViewById(R.id.card_stack_view);
        swipeLeftIcon = view.findViewById(R.id.swipeLeftIcon);
        swipeRightIcon = view.findViewById(R.id.swipeRightIcon);

        if (cardStackView == null) {
            Toast.makeText(requireContext(), "CardStackView not found in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        matchesRef = FirebaseDatabase.getInstance().getReference("matches");

        checkStoragePermission();
        setupCardStackView();
        loadPets();

        return view;
    }

    private void setupCardStackView() {
        layoutManager = new CardStackLayoutManager(requireContext(), new CardStackListener() {
            @Override
            public void onCardSwiped(Direction direction) {
                int position = layoutManager.getTopPosition() - 1;
                if (position >= 0 && position < petList.size()) {
                    Pet swipedPet = petList.get(position);
                    petList.remove(position);
                    adapter.notifyItemRemoved(position);

                    if (direction == Direction.Right) {
                        swipeRightIcon.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Liked " + swipedPet.getName(), Toast.LENGTH_SHORT).show();
                        saveMatch(swipedPet);
                    } else if (direction == Direction.Left) {
                        swipeLeftIcon.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Passed " + swipedPet.getName(), Toast.LENGTH_SHORT).show();
                    }

                    if (petList.isEmpty()) {
                        Toast.makeText(requireContext(), "No more pets to swipe!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCardDragging(Direction direction, float ratio) {
                swipeLeftIcon.setVisibility(View.GONE);
                swipeRightIcon.setVisibility(View.GONE);
            }

            @Override
            public void onCardRewound() {}

            @Override
            public void onCardCanceled() {}

            @Override
            public void onCardAppeared(View view, int position) {}

            @Override
            public void onCardDisappeared(View view, int position) {}
        });

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
                        // Fetch owner name if needed (e.g., from users node)
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

        // Fetch owner name from users node
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(likedPet.getOwnerId());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ownerName = snapshot.child("name").getValue(String.class);
                if (ownerName != null) {
                    likedPet.setOwnerName(ownerName);
                }

                Map<String, Object> matchData = new HashMap<>();
                matchData.put("userId", currentUserId);
                matchData.put("petId", likedPet.getId());
                matchData.put("petOwnerId", likedPet.getOwnerId());
                matchData.put("petName", likedPet.getName());
                matchData.put("ownerName", likedPet.getOwnerName());
                matchData.put("petImageUrl", likedPet.getImageUrl());
                matchData.put("timestamp", System.currentTimeMillis());
                matchData.put("status", "pending");

                matchesRef.push().setValue(matchData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Match saved!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save match: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch owner name: " + error.getMessage());
                Toast.makeText(requireContext(), "Failed to fetch owner name: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (petsListener != null) {
            petsRef.removeEventListener(petsListener);
        }
    }
}