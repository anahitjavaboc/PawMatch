package com.anahit.pawmatch.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.ChatRoom;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<ChatRoom> chatRoomList;
    private OnChatRoomClickListener listener;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }

    public ChatRoomAdapter(List<ChatRoom> chatRoomList, OnChatRoomClickListener listener) {
        this.chatRoomList = chatRoomList != null ? chatRoomList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomList.get(position);

        // Bind pet image
        if (chatRoom.getPetImageUrl() != null && !chatRoom.getPetImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(chatRoom.getPetImageUrl())
                    .placeholder(R.drawable.pawmatchlogo)
                    .error(R.drawable.pawmatchlogo)
                    .into(holder.petImageView);
        } else {
            holder.petImageView.setImageResource(R.drawable.pawmatchlogo);
        }

        // Bind text fields
        holder.petNameTextView.setText(chatRoom.getPetName() != null ? chatRoom.getPetName() : "Unknown Pet");
        holder.ownerNameTextView.setText(chatRoom.getOtherUserName() != null ? chatRoom.getOtherUserName() : "Unknown Owner");

        // Format and bind timestamp
        String timestampStr = "Last Message: Unknown";
        if (chatRoom.getTimestamp() != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            timestampStr = "Last Message: " + sdf.format(new Date(chatRoom.getTimestamp()));
        }
        holder.timestampTextView.setText(timestampStr);

        // Bind status
        holder.statusTextView.setText("Status: " + (chatRoom.getStatus() != null ? chatRoom.getStatus() : "Active"));

        // Set click listener
        holder.itemView.setOnClickListener(v -> listener.onChatRoomClick(chatRoom));
    }

    @Override
    public int getItemCount() {
        return chatRoomList != null ? chatRoomList.size() : 0;
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        ImageView petImageView;
        TextView petNameTextView;
        TextView ownerNameTextView;
        TextView timestampTextView;
        TextView statusTextView;

        ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            petImageView = itemView.findViewById(R.id.chat_room_pet_image);
            petNameTextView = itemView.findViewById(R.id.chat_room_pet_name);
            ownerNameTextView = itemView.findViewById(R.id.chat_room_owner_name);
            timestampTextView = itemView.findViewById(R.id.chat_room_timestamp);
            statusTextView = itemView.findViewById(R.id.chat_room_status);
        }
    }
}