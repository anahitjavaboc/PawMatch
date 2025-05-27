package com.anahit.pawmatch.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.adapters.PetCardAdapter;
import com.anahit.pawmatch.models.ChatRoom;
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
import java.util.List;

public class FeedFragment extends Fragment implements CardStackListener {

    private static final String TAG = "FeedFragment";

    private CardStackView cardStackView;
    private PetCardAdapter adapter;
    private List<Pet> petList = new ArrayList<>();
    private DatabaseReference petsRef;
    private DatabaseReference matchesRef;
    private DatabaseReference chatRoomsRef;
    private DatabaseReference chatsRef;
    private ValueEventListener petsListener;
    private CardStackLayoutManager layoutManager;
    private String currentUserId;
    private Button rulesInfoButton;
    private TextView rulesTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        cardStackView = view.findViewById(R.id.card_stack_view);
        rulesInfoButton = view.findViewById(R.id.rules_info_button);
        rulesTextView = view.findViewById(R.id.rulesTextView);

        if (cardStackView == null) {
            Log.e(TAG, "CardStackView not found in layout");
            Toast.makeText(requireContext(), "CardStackView not found in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "User not authenticated");
            Toast.makeText(requireContext(), "Please log in to swipe pets", Toast.LENGTH_LONG).show();
            return view;
        }
        currentUserId = currentUser.getUid();
        Log.d(TAG, "Current user ID: " + currentUserId);

        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        matchesRef = FirebaseDatabase.getInstance().getReference("matches");
        chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms");
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        setupCardStackView();
        loadPets();
        setupRulesButton();

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

    private void setupRulesButton() {
        if (rulesInfoButton == null || rulesTextView == null) {
            Log.e(TAG, "Rules button or text view not found in layout");
            Toast.makeText(requireContext(), "Rules UI not properly initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        rulesInfoButton.setOnClickListener(v -> {
            if (rulesTextView.getVisibility() == View.GONE) {
                rulesTextView.setVisibility(View.VISIBLE);
                rulesTextView.setText("Swipe right to like a pet, swipe left to dislike. Matches are based on pet compatibility!");
                Log.d(TAG, "Rules text displayed");
            } else {
                rulesTextView.setVisibility(View.GONE);
                Log.d(TAG, "Rules text hidden");
            }
        });
    }

    private void saveMatch(Pet likedPet) {
        if (currentUserId == null) {
            Log.e(TAG, "Cannot save match: currentUserId is null");
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        if (likedPet == null || likedPet.getId() == null) {
            Log.e(TAG, "Invalid pet data for match");
            Toast.makeText(requireContext(), "Invalid pet data", Toast.LENGTH_SHORT).show();
            return;
        }

        String matchId = matchesRef.push().getKey();
        if (matchId == null) {
            Log.e(TAG, "Error generating match ID");
            Toast.makeText(requireContext(), "Error generating match ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Match match = new Match(
                matchId,
                currentUserId,
                likedPet.getId(),
                likedPet.getOwnerId(),
                likedPet.getName(),
                likedPet.getOwnerName(),
                likedPet.getImageUrl(),
                System.currentTimeMillis(),
                "pending"
        );
        Log.d(TAG, "Match object: id=" + match.getId() + ", userId=" + match.getUserId() +
                ", petId=" + match.getPetId() + ", petOwnerId=" + match.getPetOwnerId() +
                ", petName=" + match.getPetName() + ", timestamp=" + match.getTimestamp() +
                ", status=" + match.getStatus());

        matchesRef.child(matchId).setValue(match)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Match saved with ID: " + matchId);
                    Toast.makeText(requireContext(), "Match saved!", Toast.LENGTH_SHORT).show();
                    checkMutualMatch(matchId, likedPet);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save match: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Failed to save match: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkMutualMatch(String matchId, Pet likedPet) {
        matchesRef.orderByChild("userId").equalTo(likedPet.getOwnerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Match otherMatch = data.getValue(Match.class);
                            if (otherMatch != null &&
                                    otherMatch.getPetOwnerId().equals(currentUserId) &&
                                    "pending".equals(otherMatch.getStatus())) {
                                // Mutual match found
                                matchesRef.child(matchId).child("status").setValue("matched")
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Mutual match confirmed for ID: " + matchId);
                                            Toast.makeText(requireContext(), "Mutual match with " + likedPet.getName() + "!", Toast.LENGTH_LONG).show();
                                            createChatRooms(likedPet);
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update match status: " + e.getMessage(), e));
                                data.getRef().child("status").setValue("matched");
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Mutual match check cancelled: " + error.getMessage(), error.toException());
                    }
                });
    }

    private void createChatRooms(Pet pet) {
        String otherUserId = pet.getOwnerId();
        String chatId = currentUserId.compareTo(otherUserId) < 0
                ? currentUserId + "_" + otherUserId
                : otherUserId + "_" + currentUserId;

        // Initialize chats node
        DatabaseReference chatRef = chatsRef.child(chatId).child("messages");
        chatRef.setValue(true) // Create empty messages node
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Chats node initialized for chatId: " + chatId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to initialize chats node: " + e.getMessage(), e));

        // Chat room for current user
        DatabaseReference currentUserChatRoomsRef = chatRoomsRef.child(currentUserId).child(chatId);
        ChatRoom currentUserChatRoom = new ChatRoom(
                chatId,
                pet.getName(),
                pet.getOwnerName(),
                pet.getImageUrl(),
                System.currentTimeMillis(),
                "Active",
                otherUserId
        );
        currentUserChatRoomsRef.setValue(currentUserChatRoom)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat room created for current user: " + chatId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to create chat room for current user: " + e.getMessage(), e));

        // Chat room for the other user
        DatabaseReference otherUserChatRoomsRef = chatRoomsRef.child(otherUserId).child(chatId);
        ChatRoom otherUserChatRoom = new ChatRoom(
                chatId,
                pet.getName(),
                pet.getOwnerName(),
                pet.getImageUrl(),
                System.currentTimeMillis(),
                "Active",
                currentUserId
        );
        otherUserChatRoomsRef.setValue(otherUserChatRoom)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat room created for other user: " + chatId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to create chat room for other user: " + e.getMessage(), e));
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
            Log.d(TAG, "Removed pets listener onDestroyView");
        }
    }
}