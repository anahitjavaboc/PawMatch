package com.anahit.pawmatch.models;

public class Match {
    private String id;
    private String userId; // Changed from user1 to align with FeedFragment
    private String petId;
    private String petOwnerId; // Changed from user2 to petOwnerId
    private String petName;
    private String ownerName;
    private String petImageUrl;
    private long timestamp;
    private String status;

    public Match() {}

    public Match(String id, String userId, String petId, String petOwnerId, String petName, String ownerName,
                 String petImageUrl, long timestamp, String status) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.petOwnerId = petOwnerId;
        this.petName = petName;
        this.ownerName = ownerName;
        this.petImageUrl = petImageUrl;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }
    public String getPetOwnerId() { return petOwnerId; }
    public void setPetOwnerId(String petOwnerId) { this.petOwnerId = petOwnerId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getPetImageUrl() { return petImageUrl; }
    public void setPetImageUrl(String petImageUrl) { this.petImageUrl = petImageUrl; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}