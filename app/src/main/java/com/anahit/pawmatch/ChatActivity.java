package com.anahit.pawmatch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.adapters.MessageAdapter;
import com.anahit.pawmatch.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ChildEventListener;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static final int MESSAGE_LIMIT = 20;

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ProgressBar loadingProgress;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private DatabaseReference chatRef;
    private String chatId;
    private String currentUserId;
    private String otherUserId;
    private ChildEventListener messageListener;
    private TextView ownerNameTextView, ownerAgeTextView, petNameTextView, petAgeTextView;
    private ImageView petImageView;
    private ProgressBar ownerAgeLoading, petAgeLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_chat);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading chat UI", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        String petName = getIntent().getStringExtra("petName");
        String otherUserName = getIntent().getStringExtra("otherUserName");
        String petImageUrl = getIntent().getStringExtra("petImageUrl");

        if (currentUserId == null || chatId == null || otherUserId == null) {
            Log.e(TAG, "Invalid chat data: currentUserId=" + currentUserId + ", chatId=" + chatId + ", otherUserId=" + otherUserId);
            Toast.makeText(this, "Invalid chat data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle("Chat with " + (petName != null ? petName : "Unknown Pet"));
        ownerNameTextView.setText(otherUserName != null ? otherUserName : "Unknown Owner");
        petNameTextView.setText(petName != null ? petName : "Unknown Pet");

        loadPetImage(petImageUrl);
        fetchOwnerAge(otherUserId);
        fetchPetAge(chatId);

        findViewById(R.id.profileHeader).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", otherUserId);
            intent.putExtra("petName", petName);
            intent.putExtra("petImageUrl", petImageUrl);
            intent.putExtra("otherUserName", otherUserName);
            startActivity(intent);
        });

        chatRef = FirebaseDatabase.getInstance().getReference()
                .child("chats").child(chatId).child("messages");

        setupRecyclerView(otherUserName);
        loadMessages();
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        loadingProgress = findViewById(R.id.loadingProgress);
        ownerNameTextView = findViewById(R.id.ownerNameTextView);
        ownerAgeTextView = findViewById(R.id.ownerAgeTextView);
        petNameTextView = findViewById(R.id.petNameTextView);
        petAgeTextView = findViewById(R.id.petAgeTextView);
        petImageView = findViewById(R.id.petImageView);
        ownerAgeLoading = findViewById(R.id.ownerAgeLoading);
        petAgeLoading = findViewById(R.id.petAgeLoading);
    }

    private void loadPetImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl)
                    .placeholder(R.drawable.pawmatchlogo)
                    .error(R.drawable.pawmatchlogo)
                    .into(petImageView);
        } else {
            petImageView.setImageResource(R.drawable.pawmatchlogo);
        }
    }

    private void fetchOwnerAge(String userId) {
        if (userId == null) {
            Log.e(TAG, "fetchOwnerAge: userId is null");
            ownerAgeTextView.setText("Age: N/A");
            ownerAgeLoading.setVisibility(View.GONE);
            return;
        }

        ownerAgeLoading.setVisibility(View.VISIBLE);
        ownerAgeTextView.setText("Age: Loading...");
        Log.d(TAG, "Fetching age for userId: " + userId);
        FirebaseDatabase.getInstance().getReference("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        ownerAgeLoading.setVisibility(View.GONE);
                        if (!snapshot.exists()) {
                            Log.e(TAG, "User data does not exist for userId: " + userId);
                            ownerAgeTextView.setText("Age: N/A");
                            return;
                        }

                        Integer age = snapshot.child("age").getValue(Integer.class);
                        if (age == null) {
                            Log.e(TAG, "Age not found for userId: " + userId);
                            ownerAgeTextView.setText("Age: N/A");
                        } else {
                            Log.d(TAG, "Age fetched successfully: " + age);
                            ownerAgeTextView.setText("Age: " + age);
                        }

                        String name = getIntent().getStringExtra("otherUserName");
                        if (messageAdapter != null) {
                            messageAdapter.setOwnerDetails(name != null ? name : "Unknown", age != null ? age.toString() : "N/A");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        ownerAgeLoading.setVisibility(View.GONE);
                        Log.e(TAG, "Failed to fetch owner age: " + error.getMessage());
                        ownerAgeTextView.setText("Age: N/A");
                    }
                });
    }

    private void fetchPetAge(String chatId) {
        if (chatId == null || currentUserId == null) {
            Log.e(TAG, "fetchPetAge: chatId or currentUserId is null (chatId: " + chatId + ", currentUserId: " + currentUserId + ")");
            petAgeTextView.setText("Pet Age: N/A");
            petAgeLoading.setVisibility(View.GONE);
            return;
        }

        petAgeLoading.setVisibility(View.VISIBLE);
        petAgeTextView.setText("Pet Age: Loading...");
        Log.d(TAG, "Fetching pet age for chatId: " + chatId + ", userId: " + currentUserId);
        FirebaseDatabase.getInstance().getReference("chatRooms")
                .child(currentUserId).child(chatId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.e(TAG, "Chat room data does not exist for userId: " + currentUserId + ", chatId: " + chatId);
                            petAgeTextView.setText("Pet Age: N/A");
                            petAgeLoading.setVisibility(View.GONE);
                            return;
                        }

                        String petId = snapshot.child("petId").getValue(String.class);
                        if (petId == null || petId.isEmpty()) {
                            Log.e(TAG, "Pet ID not found for chatId: " + chatId);
                            petAgeTextView.setText("Pet Age: N/A");
                            petAgeLoading.setVisibility(View.GONE);
                            return;
                        }

                        FirebaseDatabase.getInstance().getReference("pets").child(petId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot petSnapshot) {
                                        petAgeLoading.setVisibility(View.GONE);
                                        if (!petSnapshot.exists()) {
                                            Log.e(TAG, "Pet data does not exist for petId: " + petId);
                                            petAgeTextView.setText("Pet Age: N/A");
                                            return;
                                        }

                                        Integer age = petSnapshot.child("age").getValue(Integer.class);
                                        if (age == null) {
                                            Log.e(TAG, "Pet age not found for petId: " + petId);
                                            petAgeTextView.setText("Pet Age: N/A");
                                        } else {
                                            Log.d(TAG, "Pet age fetched successfully: " + age);
                                            petAgeTextView.setText("Pet Age: " + age);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        petAgeLoading.setVisibility(View.GONE);
                                        Log.e(TAG, "Failed to fetch pet age: " + error.getMessage());
                                        petAgeTextView.setText("Pet Age: N/A");
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        petAgeLoading.setVisibility(View.GONE);
                        Log.e(TAG, "Failed to fetch chat room data: " + error.getMessage());
                        petAgeTextView.setText("Pet Age: N/A");
                    }
                });
    }

    private void setupRecyclerView(String ownerName) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(this, messages);
        messageAdapter.setOwnerDetails(ownerName != null ? ownerName : "Unknown", "N/A");
        recyclerView.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        showLoading(true);
        Query recentMessagesQuery = chatRef.orderByChild("timestamp").limitToLast(MESSAGE_LIMIT);
        messageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    message.setId(snapshot.getKey());
                    messages.add(message);
                    messageAdapter.notifyItemInserted(messages.size() - 1);
                    recyclerView.scrollToPosition(messages.size() - 1);
                    Log.d(TAG, "Message loaded: " + message.getContent());
                } else {
                    Log.w(TAG, "Null message received at key: " + snapshot.getKey());
                }
                if (messages.size() > 0) {
                    showLoading(false);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load messages: " + error.getMessage());
                Toast.makeText(ChatActivity.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_LONG).show();
                showLoading(false);
            }
        };
        recentMessagesQuery.addChildEventListener(messageListener);
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = chatRef.push().getKey();
        if (messageId == null) {
            Log.e(TAG, "Failed to generate message ID");
            Toast.makeText(this, "Failed to send message: Unable to generate message ID", Toast.LENGTH_LONG).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        Message message = new Message(currentUserId, otherUserId, content, timestamp);

        chatRef.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Message sent successfully: " + messageId);
                    messageInput.setText("");
                    updateChatRoomTimestamp();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Send failed: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateChatRoomTimestamp() {
        long time = System.currentTimeMillis();
        DatabaseReference userChatRef = FirebaseDatabase.getInstance().getReference("chatRooms");
        userChatRef.child(currentUserId).child(chatId).child("timestamp").setValue(time)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update timestamp for current user: " + e.getMessage()));
        userChatRef.child(otherUserId).child(chatId).child("timestamp").setValue(time)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update timestamp for other user: " + e.getMessage()));
    }

    private void showLoading(boolean loading) {
        loadingProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        messageInput.setEnabled(true);
        sendButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRef != null && messageListener != null) {
            chatRef.removeEventListener(messageListener);
            Log.d(TAG, "Message listener removed");
        }
    }
}