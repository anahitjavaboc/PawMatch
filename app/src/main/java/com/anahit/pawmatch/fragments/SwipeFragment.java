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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe, container, false);

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
        layoutManager = new CardStackLayoutManager(requireContext(), new CardStackListener() {
            @Override
            public void onCardSwiped(Direction direction) {
                int position = layoutManager.getTopPosition() - 1;
                if (position >= 0 && position < petList.size()) {
                    Pet swipedPet = petList.get(position);
                    petList.remove(position);
                    adapter.notifyItemRemoved(position);

                    if (direction == Direction.Right) {
                        Toast.makeText(requireContext(), "Liked " + (swipedPet.getName() != null ? swipedPet.getName() : "Unknown Pet"), Toast.LENGTH_SHORT).show();
                        saveMatch(swipedPet);
                    } else if (direction == Direction.Left) {
                        Toast.makeText(requireContext(), "Passed " + (swipedPet.getName() != null ? swipedPet.getName() : "Unknown Pet"), Toast.LENGTH_SHORT).show();
                    }

                    if (petList.isEmpty()) {
                        Toast.makeText(requireContext(), "No more pets to swipe!", Toast.LENGTH_LONG).show();
                    }
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
        });

        SwipeAnimationSetting swipeRightSetting = new SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .build();
        layoutManager.setSwipeAnimationSetting(swipeRightSetting);

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

        Map<String, Object> matchData = new HashMap<>();
        matchData.put("userId", currentUserId);
        matchData.put("petId", likedPet.getId());
        matchData.put("petOwnerId", likedPet.getOwnerId());
        matchData.put("timestamp", System.currentTimeMillis());

        matchesRef.push().setValue(matchData)
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Match saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save match: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void reloadPets() {
        loadPets();
    }

    public void addPetToFirst() {
        if (petList.isEmpty()) {
            Toast.makeText(requireContext(), "No pets to add", Toast.LENGTH_SHORT).show();
            return;
        }
        Pet newPet = new Pet("New Pet", "Unknown", FirebaseAuth.getInstance().getCurrentUser().getUid(), "");
        petList.add(0, newPet);
        adapter.notifyItemInserted(0);
    }

    public void addPetToLast() {
        if (petList.isEmpty()) {
            Toast.makeText(requireContext(), "No pets to add", Toast.LENGTH_SHORT).show();
            return;
        }
        Pet newPet = new Pet("New Pet", "Unknown", FirebaseAuth.getInstance().getCurrentUser().getUid(), "");
        petList.add(newPet);
        adapter.notifyItemInserted(petList.size() - 1);
    }

    public void removePetFromFirst() {
        if (petList.isEmpty()) {
            Toast.makeText(requireContext(), "No pets to remove", Toast.LENGTH_SHORT).show();
            return;
        }
        petList.remove(0);
        adapter.notifyItemRemoved(0);
    }

    public void removePetFromLast() {
        if (petList.isEmpty()) {
            Toast.makeText(requireContext(), "No pets to remove", Toast.LENGTH_SHORT).show();
            return;
        }
        int lastPosition = petList.size() - 1;
        petList.remove(lastPosition);
        adapter.notifyItemRemoved(lastPosition);
    }

    public void replaceFirstPet() {
        if (petList.isEmpty()) {
            Toast.makeText(requireContext(), "No pets to replace", Toast.LENGTH_SHORT).show();
            return;
        }
        petList.set(0, new Pet("Replaced Pet", "Unknown", FirebaseAuth.getInstance().getCurrentUser().getUid(), ""));
        adapter.notifyItemChanged(0);
    }

    public void swapFirstForLast() {
        if (petList.size() < 2) {
            Toast.makeText(requireContext(), "Need at least two pets to swap", Toast.LENGTH_SHORT).show();
            return;
        }
        Pet first = petList.get(0);
        Pet last = petList.get(petList.size() - 1);
        petList.set(0, last);
        petList.set(petList.size() - 1, first);
        adapter.notifyItemMoved(0, petList.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (petsListener != null) {
            petsRef.removeEventListener(petsListener);
        }
    }
}