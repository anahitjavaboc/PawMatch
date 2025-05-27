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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.ChatActivity;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.adapters.ChatRoomAdapter;
import com.anahit.pawmatch.models.ChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MatchesFragment extends Fragment {

    private static final String TAG = "MatchesFragment";
    private RecyclerView recyclerView;
    private ChatRoomAdapter adapter;
    private List<ChatRoom> chatRoomList = new ArrayList<>();
    private DatabaseReference chatRoomsRef;
    private String currentUserId;
    private ValueEventListener chatRoomsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        recyclerView = view.findViewById(R.id.matchesRecyclerView);
        if (recyclerView == null) {
            Toast.makeText(requireContext(), "RecyclerView not found in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChatRoomAdapter(requireContext(), chatRoomList);
        recyclerView.setAdapter(adapter);

        // Set click listener for items with validation
        adapter.setOnItemClickListener(chatRoom -> {
            if (chatRoom.getChatId() == null || chatRoom.getOtherUserId() == null) {
                Toast.makeText(requireContext(), "Invalid chat data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "ChatRoom data incomplete: chatId=" + chatRoom.getChatId() + ", otherUserId=" + chatRoom.getOtherUserId());
                return;
            }
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("chatId", chatRoom.getChatId());
            intent.putExtra("otherUserId", chatRoom.getOtherUserId());
            intent.putExtra("petName", chatRoom.getPetName() != null ? chatRoom.getPetName() : "Unknown Pet");
            intent.putExtra("otherUserName", chatRoom.getOtherUserName() != null ? chatRoom.getOtherUserName() : "Unknown Owner");
            intent.putExtra("petImageUrl", chatRoom.getPetImageUrl() != null ? chatRoom.getPetImageUrl() : "");
            startActivity(intent);
        });

        chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms").child(currentUserId);
        fetchChatRooms();
        return view;
    }

    private void fetchChatRooms() {
        chatRoomsListener = chatRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRoomList.clear();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    ChatRoom chatRoom = chatSnapshot.getValue(ChatRoom.class);
                    if (chatRoom != null) {
                        chatRoom.setChatId(chatSnapshot.getKey());
                        chatRoomList.add(chatRoom);
                    }
                }
                if (chatRoomList.isEmpty()) {
                    Toast.makeText(requireContext(), "No chats found", Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch chat rooms: " + error.getMessage());
                Toast.makeText(requireContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatRoomsListener != null) {
            chatRoomsRef.removeEventListener(chatRoomsListener);
        }
    }
}