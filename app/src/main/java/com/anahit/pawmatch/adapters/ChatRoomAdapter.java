package com.anahit.pawmatch.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.ChatActivity;
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
    private Context context;
    private OnItemClickListener onItemClickListener;

    public ChatRoomAdapter(Context context, List<ChatRoom> chatRoomList) {
        this.context = context;
        this.chatRoomList = chatRoomList != null ? chatRoomList : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomList.get(position);

        Glide.with(context)
                .load(chatRoom.getPetImageUrl())
                .placeholder(R.drawable.pawmatchlogo)
                .error(R.drawable.pawmatchlogo)
                .fallback(R.drawable.pawmatchlogo)
                .into(holder.petImageView);

        holder.petNameTextView.setText(chatRoom.getPetName() != null ? chatRoom.getPetName() : "Unknown Pet");
        holder.ownerNameTextView.setText(chatRoom.getOtherUserName() != null ? chatRoom.getOtherUserName() : "Unknown Owner");

        if (chatRoom.getTimestamp() > System.currentTimeMillis()) {
            chatRoom.setTimestamp(System.currentTimeMillis());
        }

        String timestampStr = "Last Message: Unknown";
        if (chatRoom.getTimestamp() != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            timestampStr = "Last Message: " + sdf.format(new Date(chatRoom.getTimestamp()));
        }
        holder.timestampTextView.setText(timestampStr);

        holder.statusTextView.setText("Status: " + (chatRoom.getStatus() != null ? chatRoom.getStatus() : "Active"));

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(chatRoom);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomList != null ? chatRoomList.size() : 0;
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        ImageView petImageView;
        TextView petNameTextView, ownerNameTextView, timestampTextView, statusTextView;

        ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            petImageView = itemView.findViewById(R.id.match_pet_image);
            petNameTextView = itemView.findViewById(R.id.match_pet_name);
            ownerNameTextView = itemView.findViewById(R.id.match_owner_name);
            timestampTextView = itemView.findViewById(R.id.match_timestamp);
            statusTextView = itemView.findViewById(R.id.match_status);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ChatRoom chatRoom);
    }
}