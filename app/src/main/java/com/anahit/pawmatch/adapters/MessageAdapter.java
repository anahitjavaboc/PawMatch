package com.anahit.pawmatch.adapters;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private String currentUserId;
    private Context context;
    private String ownerName = "Unknown Owner";
    private String ownerAge = "N/A";
    private static final String TAG = "MessageAdapter";

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList != null ? messageList : new ArrayList<>();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }

    public void setOwnerDetails(String ownerName, String ownerAge) {
        this.ownerName = ownerName != null ? ownerName : "Unknown Owner";
        this.ownerAge = ownerAge != null ? ownerAge : "N/A";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        if (holder == null || position < 0 || position >= messageList.size()) {
            Log.e(TAG, "Invalid holder or position: " + position);
            return;
        }

        Message message = messageList.get(position);
        if (message == null) {
            Log.e(TAG, "Message at position " + position + " is null");
            return;
        }

        boolean isSentByCurrentUser = message.getSenderId() != null && message.getSenderId().equals(currentUserId);
        if (holder.messageContainer != null) {
            holder.messageContainer.setGravity(isSentByCurrentUser ? Gravity.END : Gravity.START);
            holder.messageContainer.setBackgroundResource(isSentByCurrentUser ? R.color.sent_message_background : R.color.received_message_background);
        } else {
            Log.e(TAG, "messageContainer is null at position: " + position);
        }

        if (holder.senderTextView != null) {
            holder.senderTextView.setText(isSentByCurrentUser ? "You" : ownerName + " (" + ownerAge + ")");
        } else {
            Log.e(TAG, "senderTextView is null at position: " + position);
        }

        if (holder.messageText != null) {
            holder.messageText.setText(message.getContent() != null ? message.getContent() : "[Empty message]");
        } else {
            Log.e(TAG, "messageText is null at position: " + position);
        }

        if (holder.timestampTextView != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.timestampTextView.setText(sdf.format(new Date(message.getTimestamp())));
        } else {
            Log.e(TAG, "timestampTextView is null at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public void updateMessages(List<Message> newMessages) {
        this.messageList = newMessages != null ? newMessages : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        TextView senderTextView, messageText, timestampTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}