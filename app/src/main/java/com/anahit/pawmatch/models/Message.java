package com.anahit.pawmatch.models;

public class Message {
    private String senderId;
    private String text;
    private long timestamp;
    private String content;
    private String receiverId; // Added to match potential five-parameter use

    public Message() {}

    public Message(String senderId, String text, long timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    // New constructor for five String parameters
    public Message(String senderId, String text, String content, String receiverId, String timestampStr) {
        this.senderId = senderId;
        this.text = text;
        this.content = content;
        this.receiverId = receiverId;
        try {
            this.timestamp = Long.parseLong(timestampStr); // Convert String to long
        } catch (NumberFormatException e) {
            this.timestamp = System.currentTimeMillis(); // Default to current time if invalid
        }
    }

    // Getters and setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
}