package com.anahit.pawmatch.models;

public class ChatRoom {
    private String chatId;
    private String petName;
    private String otherUserName;
    private String petImageUrl;
    private long timestamp;
    private String status;
    private String otherUserId;
    private String petId;

    public ChatRoom() {}

    public ChatRoom(String chatId, String petName, String otherUserName, String petImageUrl, long timestamp, String status, String otherUserId) {
        this.chatId = chatId != null ? chatId : "";
        this.petName = petName != null ? petName : "Unknown Pet";
        this.otherUserName = otherUserName != null ? otherUserName : "Unknown Owner";
        this.petImageUrl = petImageUrl != null ? petImageUrl : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.status = status != null ? status : "Active";
        this.otherUserId = otherUserId != null ? otherUserId : "";
        this.petId = "";
    }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId != null ? chatId : ""; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName != null ? petName : "Unknown Pet"; }
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName != null ? otherUserName : "Unknown Owner"; }
    public String getPetImageUrl() { return petImageUrl; }
    public void setPetImageUrl(String petImageUrl) { this.petImageUrl = petImageUrl != null ? petImageUrl : ""; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis(); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status != null ? status : "Active"; }
    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId != null ? otherUserId : ""; }
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId != null ? petId : ""; }
}