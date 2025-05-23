package com.anahit.pawmatch.models;

public class ChatRoom {
    private String user1Id;
    private String user2Id;
    private String petId;
    private String petName;
    private String petImageUrl;
    private String otherUserName;
    private String id;
    private long timestamp; // New field for last message timestamp
    private String status;  // New field for chat room status

    public ChatRoom() {}

    public ChatRoom(String user1Id, String user2Id, String petId, String petName, String petImageUrl, String otherUserName, String id, long timestamp, String status) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.petId = petId;
        this.petName = petName;
        this.petImageUrl = petImageUrl;
        this.otherUserName = otherUserName;
        this.id = id;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters
    public String getUser1Id() { return user1Id; }
    public void setUser1Id(String user1Id) { this.user1Id = user1Id; }
    public String getUser2Id() { return user2Id; }
    public void setUser2Id(String user2Id) { this.user2Id = user2Id; }
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getPetImageUrl() { return petImageUrl; }
    public void setPetImageUrl(String petImageUrl) { this.petImageUrl = petImageUrl; }
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}