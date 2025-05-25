package com.anahit.pawmatch.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.anahit.pawmatch.ChatActivity;
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

public class MatchesFragment extends Fragment implements CardStackListener {

    private static final String TAG = "MatchesFragment";
    private CardStackView cardStackView;
    private PetCardAdapter adapter;
    private List<Pet> matchPetList = new ArrayList<>();
    private DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference("matches");
    private DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("pets");
    private String currentUserId;
    private ValueEventListener matchesListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        cardStackView = view.findViewById(R.id.matchesRecyclerView);
        if (cardStackView == null) {
            Toast.makeText(requireContext(), "CardStackView not found in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return view;
        }

        CardStackLayoutManager layoutManager = new CardStackLayoutManager(requireContext(), this);
        layoutManager.setDirections(Direction.HORIZONTAL);
        layoutManager.setSwipeThreshold(0.3f);
        layoutManager.setMaxDegree(20.0f);
        layoutManager.setTranslationInterval(8.0f);
        cardStackView.setLayoutManager(layoutManager);

        adapter = new PetCardAdapter(requireContext(), matchPetList);
        cardStackView.setAdapter(adapter);

        fetchMatches();

        return view;
    }

    private void fetchMatches() {
        matchesListener = matchesRef.orderByChild("petOwnerId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        matchPetList.clear();
                        for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                            Match match = matchSnapshot.getValue(Match.class);
                            if (match != null && "matched".equals(match.getStatus())) {
                                String petId = match.getPetId();
                                if (petId != null) {
                                    petsRef.child(petId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot petSnapshot) {
                                            Pet pet = petSnapshot.getValue(Pet.class);
                                            if (pet != null) {
                                                pet.setId(petSnapshot.getKey());
                                                pet.setImageUrl(match.getPetImageUrl()); // Use match data
                                                pet.setOwnerName(match.getOwnerName()); // Use match data
                                                if (!matchPetList.contains(pet)) {
                                                    matchPetList.add(pet);
                                                }
                                                adapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, "Failed to fetch pet: " + error.getMessage());
                                        }
                                    });
                                }
                            }
                        }
                        if (matchPetList.isEmpty()) {
                            Toast.makeText(requireContext(), "No matches found", Toast.LENGTH_LONG).show();
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch matches: " + error.getMessage());
                        Toast.makeText(requireContext(), "Failed to load matches", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (adapter == null || matchPetList.isEmpty()) return;

        int position = ((CardStackLayoutManager) cardStackView.getLayoutManager()).getTopPosition() - 1;
        if (position < 0 || position >= matchPetList.size()) return;

        Pet pet = matchPetList.get(position);
        if (direction == Direction.Right) {
            Toast.makeText(requireContext(), "Starting chat with " + (pet.getName() != null ? pet.getName() : "pet"), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("petId", pet.getId());
            intent.putExtra("ownerId", pet.getOwnerId());
            startActivity(intent);
        } else if (direction == Direction.Left) {
            Toast.makeText(requireContext(), "Skipped " + (pet.getName() != null ? pet.getName() : "pet"), Toast.LENGTH_SHORT).show();
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
        if (matchesListener != null) {
            matchesRef.removeEventListener(matchesListener);
        }
    }
}